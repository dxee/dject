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
    private static final Logger LOGGER = LoggerFactory.getLogger(JSR250PreDestroyLifecycleFeature.class);

    @Override
    public PreDestroyTypeVisitor visitor() {
        return new PreDestroyTypeVisitor(this.annotationClazz);
    }

    private class PreDestroyTypeVisitor extends AbstractTypeVisitor {
        public PreDestroyTypeVisitor(Class<? extends Annotation> annotationClazz) {
            super(annotationClazz);
        }

        @Override
        public void addMethodLifecycleAction(LifecycleAction lifecycleAction) {
            addLifecycleActionToLastOne(lifecycleAction);
        }

        @Override
        public boolean visit(final Class<?> clazz) {
            boolean continueVisit = !clazz.isInterface();
            if (continueVisit && AutoCloseable.class.isAssignableFrom(clazz)) {
                AutoCloseableLifecycleAction closeableAction = new AutoCloseableLifecycleAction(
                        clazz.asSubclass(AutoCloseable.class));
                LOGGER.debug("adding action {}", closeableAction);
                addLifecycleActionToLastOne(closeableAction);
                continueVisit = false;
            }
            return continueVisit;
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
