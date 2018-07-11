package com.github.dxee.dject.lifecycle.impl;

import com.github.dxee.dject.internal.JSR250LifecycleAction;
import com.github.dxee.dject.internal.TypeInspector;
import com.github.dxee.dject.lifecycle.LifecycleAction;
import com.github.dxee.dject.spi.LifecycleFeature;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Special LifecycleFeature to support @PostConstruct annotation processing.
 * Note that this feature is implicit in LifecycleModule and therefore does not
 * need to be added using the LifecycleFeature multibinding.
 *
 * @author elandau
 */
public final class PostConstructLifecycleFeature implements LifecycleFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostConstructLifecycleFeature.class);
    private final JSR250LifecycleAction.ValidationMode validationMode;

    public PostConstructLifecycleFeature(JSR250LifecycleAction.ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    @Override
    public List<LifecycleAction> getActionsForType(final Class<?> type) {
        return TypeInspector.accept(type, new PostConstructVisitor());
    }

    @Override
    public String toString() {
        return "PostConstruct";
    }

    private class PostConstructVisitor implements TypeInspector.TypeVisitor, Supplier<List<LifecycleAction>> {
        private final Set<String> visitContext;
        private final LinkedList<LifecycleAction> typeActions;

        public PostConstructVisitor() {
            this.visitContext = new HashSet<>();
            this.typeActions = new LinkedList<>();
        }

        @Override
        public boolean visit(final Class<?> clazz) {
            return !clazz.isInterface();
        }

        @Override
        public boolean visit(final Method method) {
            final String methodName = method.getName();
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (!visitContext.contains(methodName)) {
                    try {
                        LifecycleAction postConstructAction = new JSR250LifecycleAction(PostConstruct.class,
                                method, validationMode);
                        LOGGER.debug("adding action {}", postConstructAction);
                        this.typeActions.addFirst(postConstructAction);
                        visitContext.add(methodName);
                    } catch (IllegalArgumentException e) {
                        LOGGER.info("ignoring @PostConstruct method {}.{}() - {}",
                                method.getDeclaringClass().getName(), methodName, e.getMessage());
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

}
