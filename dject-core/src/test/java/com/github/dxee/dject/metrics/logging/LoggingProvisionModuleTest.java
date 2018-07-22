package com.github.dxee.dject.metrics.logging;

import com.github.dxee.dject.Dject;
import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import org.junit.Assert;
import org.junit.Test;

public class LoggingProvisionModuleTest {
    @Test
    public void confirmListenerExists() {
        Dject injector = Dject.newBuilder()
                .withModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                    }
                })
                .withLoggingProvision()
                .withStage(Stage.PRODUCTION)
                .build();
        LoggingProvisionMetricsLifecycleListener loggingProvisionMetricsLifecycleListener
                = injector.getInstance(LoggingProvisionMetricsLifecycleListener.class);
        Assert.assertNotNull(loggingProvisionMetricsLifecycleListener);

    }
}
