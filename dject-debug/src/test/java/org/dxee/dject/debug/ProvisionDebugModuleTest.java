package org.dxee.dject.debug;

import com.google.inject.AbstractModule;
import org.dxee.dject.InjectorBuilder;
import org.dxee.dject.lifecycle.LifecycleInjector;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

public class ProvisionDebugModuleTest {
    @Test
    public void confirmListenerExists() {
        try (LifecycleInjector injector = InjectorBuilder.fromModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new ProvisionDebugModule());
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
