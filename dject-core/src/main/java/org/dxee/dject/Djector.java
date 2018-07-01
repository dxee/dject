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


    public static Djector createInjector(Stage stage, Module module) {
        final LifecycleManager manager = new LifecycleManager();
        // Construct the injector using our override structure
        try {
            Injector injector = Guice.createInjector(
                    stage,
                    // This has to be first to make sure @PostConstruct support is added as early
                    // as possible
                    new ProvisionMetricsModule(),
                    new LifecycleModule(),
                    new LifecycleListenerModule(),
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(Djector.class);
                            bind(LifecycleManager.class).toInstance(manager);
                        }
                    },
                    module
            );
            manager.notifyStarted();
            Djector djector = injector.getExistingBinding(Key.get(Djector.class)).getProvider().get();
            LOGGER.info("Injector created successfully ");
            return djector;
        } catch (Exception e) {
            LOGGER.error("Failed to create injector - {}@{}",
                    e.getClass().getSimpleName(),
                    System.identityHashCode(e),
                    e);
            try {
                manager.notifyStartFailed(e);
            } catch (Exception e2) {
                LOGGER.error("Failed to notify injector creation failure", e2);
            }
            throw e;
        }
    }
}
