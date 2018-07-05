package com.github.dxee.dject.extend;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.dxee.dject.lifecycle.LifecycleShutdown;
import com.google.inject.AbstractModule;

/**
 * When installed ShutdownHookModule will link a JVM shutdown hook to
 * LifecycleManager so that calling System.exit() will shutdown 
 * it down.
 * 
 * <pre>
 * {@code
 *    Dject.builder().withModule(new ShutdownHookModule());
 * }
 * </pre>
 */
public final class ShutdownHookModule extends AbstractModule {
    @Singleton
    public static class SystemShutdownHook extends Thread {
        @Inject
        public SystemShutdownHook(final LifecycleShutdown shutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown.shutdown()));
        }
    }
    
    @Override
    protected void configure() {
        bind(SystemShutdownHook.class).asEagerSingleton();
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ShutdownHookModule[]";
    }

}
