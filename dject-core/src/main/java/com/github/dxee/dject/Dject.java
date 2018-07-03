package com.github.dxee.dject;

import com.github.dxee.dject.lifecycle.LifecycleShutdown;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wrapper for Guice's Injector with extended methods.
 */
@Singleton
public final class Dject extends DelegatingInjector implements AutoCloseable {
    private final LifecycleShutdown lifecycleShutdown;

    @Inject
    public Dject(Injector injector, LifecycleShutdown lifecycleShutdown) {
        super(injector);
        this.lifecycleShutdown = lifecycleShutdown;
    }

    /**
     * Block until LifecycleManager terminates
     *
     * @throws InterruptedException
     */
    public void awaitTermination() throws InterruptedException {
        lifecycleShutdown.awaitTermination();
    }

    @Override
    public void close() {
        lifecycleShutdown.shutdown();
    }
}
