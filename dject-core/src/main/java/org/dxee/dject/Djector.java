package org.dxee.dject;

import com.google.inject.*;
import org.dxee.dject.lifecycle.LifecycleListenerModule;
import org.dxee.dject.lifecycle.LifecycleManager;
import org.dxee.dject.lifecycle.LifecycleModule;
import org.dxee.dject.lifecycle.LifecycleShutdown;
import org.dxee.dject.metrics.ProvisionMetricsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wrapper for Guice's Injector with extended methods.
 */
@Singleton
public final class Djector extends DelegatingInjector implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Djector.class);
    private final LifecycleManager manager;
    private final LifecycleShutdown lifecycleShutdown;

    @Inject
    public Djector(Injector injector, LifecycleManager manager, LifecycleShutdown lifecycleShutdown) {
        super(injector);
        this.manager = manager;
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
