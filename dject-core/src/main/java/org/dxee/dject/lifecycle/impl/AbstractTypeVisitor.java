package org.dxee.dject.lifecycle.impl;

import org.dxee.dject.lifecycle.LifecycleAction;
import org.dxee.dject.lifecycle.SimpleLifecycleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

/**
 * This is a abstract visitor implement.
 * @author bing.fan
 * 2018-06-07 19:42
 */
public abstract class AbstractTypeVisitor implements TypeInspector.TypeVisitor, Supplier<List<LifecycleAction>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTypeVisitor.class);

    private Set<String> visitContext;
    private LinkedList<LifecycleAction> lifecycleActions;
    private Class<? extends Annotation> annotationClazz;

    public AbstractTypeVisitor(Class<? extends Annotation> annotationClazz) {
        this.visitContext = new HashSet<>();
        this.lifecycleActions = new LinkedList<>();
        this.annotationClazz = annotationClazz;

    }

    @Override
    public boolean visit(final Class<?> clazz) {
        return !clazz.isInterface();
    }

    @Override
    public boolean visit(final Method method) {
        final String methodName = method.getName();
        if (method.isAnnotationPresent(annotationClazz)) {
            if (!visitContext.contains(methodName)) {
                try {
                    LifecycleAction lifecycleAction = new SimpleLifecycleAction(annotationClazz, method);
                    LOGGER.debug("adding action {}", lifecycleAction);
                    addMethodLifecycleAction(lifecycleAction);
                    visitContext.add(methodName);
                } catch (IllegalArgumentException e) {
                    LOGGER.info("ignoring @{} method {}.{}() - {}", annotationClazz.getSimpleName(), method.getDeclaringClass().getName(),
                            methodName, e.getMessage());
                }
            }
        } else if (method.getReturnType() == Void.TYPE && method.getParameterTypes().length == 0 && !Modifier.isFinal(method.getModifiers())) {
            // method potentially overrides superclass method and annotations
            visitContext.add(methodName);
        }
        return true;
    }

    public void addLifecycleActionToFirstOne(LifecycleAction lifecycleAction) {
        lifecycleActions.addFirst(lifecycleAction);
    }

    public void addLifecycleActionToLastOne(LifecycleAction lifecycleAction) {
        lifecycleActions.add(lifecycleAction);
    }

    abstract public void addMethodLifecycleAction(LifecycleAction lifecycleAction);

    @Override
    public boolean visit(Field field) {
        return true;
    }

    @Override
    public List<LifecycleAction> get() {
        return Collections.unmodifiableList(lifecycleActions);
    }
}
