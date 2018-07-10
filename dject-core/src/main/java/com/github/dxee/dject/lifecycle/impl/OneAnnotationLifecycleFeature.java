package com.github.dxee.dject.lifecycle.impl;

import com.github.dxee.dject.lifecycle.LifecycleAction;
import com.github.dxee.dject.lifecycle.LifecycleFeature;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Supplier;

/**
 * OneAnnotationLifecycleFeature
 * @author bing.fan
 * 2018-06-08 20:15
 */
public abstract class OneAnnotationLifecycleFeature implements LifecycleFeature {
    protected Class<? extends Annotation> annotationClazz;

    public OneAnnotationLifecycleFeature() {
        this.annotationClazz = annotationClazz();
    }

    @Override
    public List<LifecycleAction> getActionsForType(final Class<?> type) {
        return TypeInspector.accept(type, visitor());
    }

    public abstract <V extends Supplier<List<LifecycleAction>> & TypeInspector.TypeVisitor> V visitor();

    @Override
    public int priority() {
        return 0;
    }

    /**
     * Lifecycle feature annotation
     * @return the annotation class
     */
    public abstract Class<? extends Annotation> annotationClazz();
}
