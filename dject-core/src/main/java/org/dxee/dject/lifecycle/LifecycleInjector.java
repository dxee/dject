package org.dxee.dject.lifecycle;

import com.google.inject.Injector;
import org.dxee.dject.DelegatingInjector;

/**
 * Wrapper for Guice's Injector with added close methods.
 *
 * <b>Invoking close from outside the injector</b>
 * <pre>
 * <code>
 *    LifecycleInjector injector = new Governator().run();
 *    // ...
 *    injector.close();
 * </code>
 * </pre>
 *
 * <b>Blocking on the injector terminating</b>
 * <pre>
 * <code>
 *    LifecycleInjector injector = new Governator().run(;
 *    // ...
 *    injector.awaitTermination();
 * </code>
 * </pre>
 *
 * <b>Triggering close from a DI'd class</b>
 * <pre>
 * <code>
 *    {@literal @}Singleton
 *    public class SomeShutdownService {
 *        {@literal @}Inject
 *        SomeShutdownService(LifecycleManager lifecycleManager) {
 *            this.lifecycleManager = lifecycleManager;
 *        }
 *
 *        void someMethodInvokedForShutdown() {
 *            this.lifecycleManager.close();
 *        }
 *    }
 * }
 * </code>
 * </pre>
 *
 * <b>Triggering an external event from shutdown without blocking</b>
 * <pre>
 * <code>
 *    LifecycleInjector injector = new Governator().run(;
 *    injector.addListener(new LifecycleListener() {
 *        public void onShutdown() {
 *            // Do your shutdown handling here
 *        }
 *    });
 * }
 * </code>
 * </pre>
 */
final public class LifecycleInjector extends DelegatingInjector implements AutoCloseable {
    private final LifecycleManager manager;
    private final LifecycleShutdownSignal signal;

    public static LifecycleInjector wrapInjector(Injector injector, LifecycleManager manager) {
        return new LifecycleInjector(injector, manager, injector.getInstance(LifecycleShutdownSignal.class));
    }

    private LifecycleInjector(Injector injector, LifecycleManager manager, LifecycleShutdownSignal signal) {
        super(injector);
        this.manager = manager;
        this.signal = signal;
    }

    /**
     * Block until LifecycleManager terminates
     *
     * @throws InterruptedException
     */
    public void awaitTermination() throws InterruptedException {
        signal.await();
    }

    /**
     * Register a single shutdown listener for async notification of the LifecycleManager
     * terminating.
     *
     * @param listener
     */
    public void addListener(LifecycleListener listener) {
        manager.addListener(listener);
    }

    @Override
    public void close() {
        signal.signal();
    }
}
