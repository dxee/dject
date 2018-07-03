package org.dxee.dject.metrics.impl;

import com.google.inject.AbstractModule;
import org.dxee.dject.Dject;
import org.dxee.dject.DjectBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

public class LoggingProvisionModuleTest {
    @Test
    public void confirmListenerExists() {
        try (Dject injector = DjectBuilder.fromModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new LoggingProvisionModule());
                    }
                })
                .createInjector()) {
            LoggingProvisionMetricsLifecycleListener loggingProvisionMetricsLifecycleListener = injector.getInstance(LoggingProvisionMetricsLifecycleListener.class);
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
