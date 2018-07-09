package com.github.dxee.dject.lifecycle;

import com.github.dxee.dject.lifecycle.impl.AbstractTypeVisitor;
import com.github.dxee.dject.lifecycle.impl.OneAnnotationLifecycleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;

/**
 * Special LifecycleFeature to support @PreDestroy annotation processing and
 * java.lang.AutoCloseable detection.
 * @author bing.fan
 * 2018-06-08 20:03
 */
public final class JSR250PreDestroyLifecycleFeature extends OneAnnotationLifecycleFeature
        implements PreDestroyLifecycleFeature {

    @Override
    public JSR250PreDestroyTypeVisitor visitor() {
        return new JSR250PreDestroyTypeVisitor(this.annotationClazz);
    }

    private class JSR250PreDestroyTypeVisitor extends AbstractTypeVisitor {
        public JSR250PreDestroyTypeVisitor(Class<? extends Annotation> annotationClazz) {
            super(annotationClazz);
        }

        @Override
        public void addMethodLifecycleAction(LifecycleAction lifecycleAction) {
            addLifecycleActionToLastOne(lifecycleAction);
        }
    }


    @Override
    public Class<? extends Annotation> annotationClazz() {
        return PreDestroy.class;
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Predestroy @")
                .append(this.annotationClazz == null ? "null" : this.annotationClazz.getSimpleName())
                .append(" with priority ").append(priority()).toString();
    }
}
