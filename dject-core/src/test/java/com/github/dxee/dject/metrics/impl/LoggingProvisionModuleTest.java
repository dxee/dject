package com.github.dxee.dject.metrics.impl;

import com.github.dxee.dject.Dject;
import com.google.inject.AbstractModule;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

public class LoggingProvisionModuleTest {
    @Test
    public void confirmListenerExists() {
        try (Dject injector = Dject.builder().withModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new LoggingProvisionModule());
                    }
                })
                .build()) {
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
