package org.dxee.dject.debug;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Stage;
import org.dxee.dject.InjectorBuilder;
import org.dxee.dject.lifecycle.LifecycleInjector;
import org.dxee.dject.metrics.LoggingProvisionMetricsVisitor;
import org.dxee.dject.metrics.ProvisionMetrics;
import org.dxee.dject.metrics.ProvisionMetricsModule;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

public class ProvisionMetricsModuleTest {
    @Test
    public void confirmDedupWorksWithOverride() {
        try (LifecycleInjector injector = InjectorBuilder.fromModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new ProvisionDebugModule());
                    }
                })
                // Confirm that installing ProvisionMetricsModule twice isn't broken with overrides
                .overrideWith(new AbstractModule() {
                    @Override
                    protected void configure() {
                    }
                })
                .createInjector()) {

            LoggingProvisionMetricsLifecycleListener loggingProvisionMetricsLifecycleListener = injector.getInstance(LoggingProvisionMetricsLifecycleListener.class);
            injector.getInstance(Foo.class);
            Assert.assertNotNull(loggingProvisionMetricsLifecycleListener);
        }
    }

    @Singleton
    public static class Foo {
        public Foo() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(200);
        }
    }
}
