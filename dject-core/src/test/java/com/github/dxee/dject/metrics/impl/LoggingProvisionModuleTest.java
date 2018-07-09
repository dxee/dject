package com.github.dxee.dject.metrics.impl;

import com.github.dxee.dject.Dject;
import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

public class LoggingProvisionModuleTest {
    @Test
    public void confirmListenerExists() {
        Dject injector = Dject.builder()
                .withModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                    }
                })
                .withLoggingProvision()
                .withStage(Stage.PRODUCTION)
                .build();
        LoggingProvisionMetricsLifecycleListener loggingProvisionMetricsLifecycleListener = injector.getInstance(LoggingProvisionMetricsLifecycleListener.class);
        Assert.assertNotNull(loggingProvisionMetricsLifecycleListener);

    }
}
