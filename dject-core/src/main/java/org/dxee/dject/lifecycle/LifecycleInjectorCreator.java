package org.dxee.dject.lifecycle;

import org.dxee.dject.InjectorCreator;
import org.dxee.dject.metrics.ProvisionMetricsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Custom strategy for creating a Guice Injector that enables support for lifecycle annotations such
 * as {@link @PreDestroy} and {@link @PostConstruct} as well as injector lifecycle hooks via the
 * {@link LifecycleListener} API.
 * <p>
 * The LifecycleInjectorCreator may be overridden to handle pre-create and post-create notification.
 */
public class LifecycleInjectorCreator implements InjectorCreator<LifecycleInjector> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleInjectorCreator.class);

    @Override
    public LifecycleInjector createInjector(Stage stage, Module module) {
        final LifecycleManager manager = new LifecycleManager();

        // Construct the injector using our override structure
        try {
            onBeforeInjectorCreate();
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
                            bind(LifecycleManager.class).toInstance(manager);
                            requestInjection(LifecycleInjectorCreator.this);
                        }
                    },
                    module
            );
            manager.notifyStarted();
            LifecycleInjector lifecycleInjector = LifecycleInjector.wrapInjector(injector, manager);
            onSuccessfulInjectorCreate();
            LOGGER.info("Injector created successfully ");
            return lifecycleInjector;
        } catch (Exception e) {
            LOGGER.error("Failed to create injector - {}@{}", e.getClass().getSimpleName(), System.identityHashCode(e), e);
            onFailedInjectorCreate(e);
            try {
                manager.notifyStartFailed(e);
            } catch (Exception e2) {
                LOGGER.error("Failed to notify injector creation failure", e2);
            }
            throw e;
        } finally {
            onCompletedInjectorCreate();
        }
    }

    /**
     * Template method invoked immediately before the injector is created
     */
    protected void onBeforeInjectorCreate() {
    }

    /**
     * Template method invoked immediately after the injector is created
     */
    protected void onSuccessfulInjectorCreate() {
    }

    /**
     * Template method invoked immediately after any failure to create the injector
     *
     * @param error Cause of the failure
     */
    protected void onFailedInjectorCreate(Throwable error) {
    }

    /**
     * Template method invoked at the end of createInjector() regardless of whether
     * the injector was created successful or not.
     */
    protected void onCompletedInjectorCreate() {

    }

    @Override
    public String toString() {
        return "LifecycleInjectorCreator[]";
    }
}
