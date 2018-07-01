package org.dxee.dject.lifecycle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Ordering;
import org.dxee.dject.lifecycle.impl.AbstractLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.ProvisionListener;
import org.dxee.dject.annotations.SuppressLifecycleUninitialized;
import org.dxee.dject.internal.PreDestroyMonitor;

/**
 * eg. Adds support for standard lifecycle annotations @PostConstruct and @PreDestroy to Guice.
 *
 * <code>
 * public class MyService {
 *      {@literal @}PostConstruct
 *      public void init() {
 *      }
 * <p>
 *      {@literal @}PreDestroy
*       public void shutdown() {
 *      }
 * }
 * </code>
 */
public final class LifecycleModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleModule.class);

    private LifecycleProvisionListener provisionListener = new LifecycleProvisionListener();

    /**
     * Holder of actions for a specific type.
     */
    static class TypeLifecycleActions {
        final List<LifecycleAction> postConstructActions = new ArrayList<LifecycleAction>();
        final List<LifecycleAction> preDestroyActions = new ArrayList<>();
    }

    @Singleton
    @SuppressLifecycleUninitialized
    static class LifecycleProvisionListener extends AbstractLifecycleListener implements ProvisionListener {
        private final ConcurrentMap<Class<?>, TypeLifecycleActions> cache = new ConcurrentHashMap<>(4096);
        private List<PostConstructLifecycleFeature> postConstructLifecycleFeatures;
        private List<PreDestroyLifecycleFeature> preDestoryLifecycleFeatures;
        private final AtomicBoolean isShutdown = new AtomicBoolean();
        private PreDestroyMonitor preDestroyMonitor;
        private boolean shutdownOnFailure = true;

        @Inject
        public static void initialize(
                final Injector injector,
                LifecycleProvisionListener provisionListener,
                Set<PostConstructLifecycleFeature> postConstructLifecycleFeatures,
                Set<PreDestroyLifecycleFeature> preDestoryLifecycleFeatures) {
            // Order by feature priority
            Ordering<LifecycleFeature> lifecycleFeatureOrdering = Ordering.from(
                    Comparator.comparingInt(LifecycleFeature::priority)
            );
            provisionListener.postConstructLifecycleFeatures = lifecycleFeatureOrdering
                    .sortedCopy(postConstructLifecycleFeatures);
            provisionListener.preDestoryLifecycleFeatures = lifecycleFeatureOrdering
                    .sortedCopy(preDestoryLifecycleFeatures);
            provisionListener.preDestroyMonitor = new PreDestroyMonitor(injector.getScopeBindings());
            LOGGER.debug("LifecycleProvisionListener initialized with postConstructLifecycleFeatures {}",
                    postConstructLifecycleFeatures);
        }

        public TypeLifecycleActions createActions(Class<?> type) {
            TypeLifecycleActions actions = cache.get(type);
            if (actions == null) {
                actions = new TypeLifecycleActions();
                for (LifecycleFeature feature : postConstructLifecycleFeatures) {
                    actions.postConstructActions.addAll(feature.getActionsForType(type));
                }

                for (LifecycleFeature feature : preDestoryLifecycleFeatures) {
                    actions.preDestroyActions.addAll(feature.getActionsForType(type));
                }

                TypeLifecycleActions existing = cache.putIfAbsent(type, actions);
                if (existing != null) {
                    return existing;
                }
            }
            return actions;
        }

        /**
         * Invoke all shutdown actions
         */
        @Override
        public synchronized void onStopped(Throwable optionalFailureReason) {
            if (shutdownOnFailure || optionalFailureReason == null) {
                if (isShutdown.compareAndSet(false, true)) {
                    try {
                        preDestroyMonitor.close();
                    } catch (Exception e) {
                        LOGGER.error("failed closing preDestroyMonitor", e);
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "LifecycleProvisionListener@" + System.identityHashCode(this);
        }

        @Override
        public <T> void onProvision(ProvisionInvocation<T> provision) {
            final T injectee = provision.provision();
            if (injectee == null) {
                return;
            }
            if (postConstructLifecycleFeatures == null) {
                if (!injectee.getClass().isAnnotationPresent(SuppressLifecycleUninitialized.class)) {
                    LOGGER.debug("LifecycleProvisionListener not initialized yet : {}", injectee.getClass());
                }

                // TODO: Add to PreDestroy list
                return;
            }

            //Ignore for Spring-managed bindings
            Object source = provision.getBinding().getSource();
            if (source != null && source.toString().contains("spring-guice")) {
                return;
            }

            final TypeLifecycleActions actions = createActions(injectee.getClass());

            // Call all postConstructActions for this injectee
            if (!actions.postConstructActions.isEmpty()) {
                try {
                    new ManagedInstanceAction(injectee, actions.postConstructActions).call();
                } catch (Exception e) {
                    throw new ProvisionException("postConstruct failed", e);
                }
            }

            // Add any PreDestroy methods to the shutdown list of actions
            if (!actions.preDestroyActions.isEmpty()) {
                if (isShutdown.get() == false) {
                    preDestroyMonitor.register(injectee, provision.getBinding(), actions.preDestroyActions);
                } else {
                    LOGGER.warn("Already shutting down.  Shutdown methods {} on {} will not be invoked",
                            actions.preDestroyActions,
                            injectee.getClass().getName()
                    );
                }
            }
        }
    }

    @Override
    protected void configure() {
        requestStaticInjection(LifecycleProvisionListener.class);
        bind(LifecycleProvisionListener.class).toInstance(provisionListener);
        bindListener(Matchers.any(), provisionListener);
        Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class);
        Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class);
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
        return "LifecycleModule@" + System.identityHashCode(this);
    }
}
