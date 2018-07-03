package com.github.dxee.dject.metrics;

import com.github.dxee.dject.Dject;
import com.github.dxee.dject.DjectBuilder;
import com.github.dxee.dject.metrics.impl.LoggingProvisionModule;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

public class ProvisionMetricsModuleTest {
    @Test
    public void disableMetrics() {
        try (Dject injector = DjectBuilder.fromModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ProvisionMetrics.class).to(NullProvisionMetrics.class);
                    }
                })
                .createInjector()) {

            ProvisionMetrics metrics = injector.getInstance(ProvisionMetrics.class);
            TestProvisionMetricsVisitor visitor = new TestProvisionMetricsVisitor();
            metrics.accept(visitor);
            Assert.assertTrue(visitor.getElementCount() == 0);
        }
    }

    @Test
    public void confirmDedupWorksWithOverride() {
        try (Dject injector = DjectBuilder.fromModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new ProvisionMetricsModule());
                    }
                })
                // Confirm that installing ProvisionMetricsModule twice isn't broken with overrides
                .overrideWith(new AbstractModule() {
                    @Override
                    protected void configure() {
                    }
                })
                .createInjector()) {

            ProvisionMetrics metrics = injector.getInstance(ProvisionMetrics.class);
            TestProvisionMetricsVisitor visitor = new TestProvisionMetricsVisitor();
            metrics.accept(visitor);
            Assert.assertTrue(visitor.getElementCount() != 0);
        }
    }

    @Singleton
    public static class Foo {
        public Foo() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(200);
        }
    }

    public class KeyTrackingVisitor implements ProvisionMetrics.Visitor {
        private ProvisionMetrics.Element element;
        private Key key;

        KeyTrackingVisitor(Key key) {
            this.key = key;
        }

        @Override
        public void visit(ProvisionMetrics.Element element) {
            if (element.getKey().equals(key)) {
                this.element = element;
            }
        }

        ProvisionMetrics.Element getElement() {
            return element;
        }
    }

    @Test
    public void confirmMetricsIncludePostConstruct() {
        try (Dject injector = DjectBuilder.fromModules(
                new LoggingProvisionModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Foo.class).asEagerSingleton();
                    }
                })
                .traceEachProvisionListener()
                .createInjector()) {

            ProvisionMetrics metrics = injector.getInstance(ProvisionMetrics.class);
            KeyTrackingVisitor keyTracker = new KeyTrackingVisitor(Key.get(Foo.class));
            metrics.accept(keyTracker);

            Assert.assertNotNull(keyTracker.getElement());
            Assert.assertTrue(keyTracker.getElement().getTotalDuration(TimeUnit.MILLISECONDS) >= 200);
        }
    }
}
