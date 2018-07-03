package com.github.dxee.dject.lifecycle;

import com.github.dxee.dject.lifecycle.impl.AbstractTypeVisitor;
import com.github.dxee.dject.lifecycle.impl.OneAnnotationLifecycleFeature;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;

/**
 * Special LifecycleFeature to support @PostConstruct annotation processing.
 * @author bing.fan
 * 2018-06-07 19:54
 */
public final class JSR250PostConstructLifecycleFeature
        extends OneAnnotationLifecycleFeature implements PostConstructLifecycleFeature {

    @Override
    public JSR250PostConstructTypeVisitor visitor() {
        return new JSR250PostConstructTypeVisitor(this.annotationClazz);
    }

    private class JSR250PostConstructTypeVisitor extends AbstractTypeVisitor {
        public JSR250PostConstructTypeVisitor(Class<? extends Annotation> annotationClazz) {
            super(annotationClazz);
        }

        @Override
        public void addMethodLifecycleAction(LifecycleAction lifecycleAction) {
            addLifecycleActionToFirstOne(lifecycleAction);
        }
    }

    @Override
    public Class<? extends Annotation> annotationClazz() {
        return PostConstruct.class;
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("PostConstruct @")
                .append(this.annotationClazz == null ? "null" : this.annotationClazz.getSimpleName())
                .append(" with priority ").append(priority()).toString();
    }
}
