package com.github.dxee.dject.lifecycle.impl;

import com.github.dxee.dject.internal.JSR250LifecycleAction;
import com.github.dxee.dject.internal.TypeInspector;
import com.github.dxee.dject.lifecycle.LifecycleAction;
import com.github.dxee.dject.spi.LifecycleFeature;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Special LifecycleFeature to support @PreDestroy annotation processing and
 * java.lang.AutoCloseable detection. Note that this feature is implicit in
 * LifecycleModule and therefore does not need to be added using the
 * LifecycleFeature multibinding.
 *
 * @author elandau
 */
public final class PreDestroyLifecycleFeature implements LifecycleFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreDestroyLifecycleFeature.class);
    private final JSR250LifecycleAction.ValidationMode validationMode;
    private final boolean preDestroyAutoCloseable;

    public PreDestroyLifecycleFeature(JSR250LifecycleAction.ValidationMode validationMode,
                                      boolean preDestroyAutoCloseable) {
        this.validationMode = validationMode;
        this.preDestroyAutoCloseable = preDestroyAutoCloseable;
    }

    @Override
    public List<LifecycleAction> getActionsForType(final Class<?> type) {
        return TypeInspector.accept(type, new PreDestroyVisitor());
    }

    @Override
    public String toString() {
        return "PreDestroy";
    }

    private class PreDestroyVisitor implements TypeInspector.TypeVisitor, Supplier<List<LifecycleAction>> {
        private Set<String> visitContext = new HashSet<>();
        private List<LifecycleAction> typeActions = new ArrayList<>();

        @Override
        public boolean visit(final Class<?> clazz) {
            boolean continueVisit = !clazz.isInterface();
            if (preDestroyAutoCloseable && continueVisit && AutoCloseable.class.isAssignableFrom(clazz)) {
                AutoCloseableLifecycleAction closeableAction = new AutoCloseableLifecycleAction(
                        clazz.asSubclass(AutoCloseable.class));
                LOGGER.debug("adding action {}", closeableAction);
                typeActions.add(closeableAction);
                continueVisit = false;
            }
            return continueVisit;
        }

        @Override
        public boolean visit(final Method method) {

            final String methodName = method.getName();
            if (method.isAnnotationPresent(PreDestroy.class)) {
                if (!visitContext.contains(methodName)) {
                    try {
                        LifecycleAction destroyAction = new JSR250LifecycleAction(PreDestroy.class,
                                method, validationMode);
                        LOGGER.debug("adding action {}", destroyAction);
                        typeActions.add(destroyAction);
                        visitContext.add(methodName);
                    } catch (IllegalArgumentException e) {
                        LOGGER.info("ignoring @PreDestroy method {}.{}() - {}", method.getDeclaringClass().getName(),
                                methodName, e.getMessage());
                    }
                }
            } else if (method.getReturnType() == Void.TYPE && method.getParameterTypes().length == 0
                    && !Modifier.isFinal(method.getModifiers())) {
                // method potentially overrides superclass method and annotations
                visitContext.add(methodName);
            }
            return true;
        }

        @Override
        public boolean visit(Field field) {
            return true;
        }

        @Override
        public List<LifecycleAction> get() {
            return Collections.unmodifiableList(typeActions);
        }
    }


    private static final class AutoCloseableLifecycleAction implements LifecycleAction {
        private final String description;

        private AutoCloseableLifecycleAction(Class<? extends AutoCloseable> clazz) {
            this.description = new StringBuilder().append("AutoCloseable@")
                    .append(System.identityHashCode(this))
                    .append("[").append(clazz.getName()).append(".").append("close()").append("]")
                    .toString();
        }

        @Override
        public void call(Object obj) throws Exception {
            LOGGER.info("calling action {}", description);
            AutoCloseable.class.cast(obj).close();
        }

        @Override
        public String toString() {
            return description;
        }
    }

}
