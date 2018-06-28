package org.dxee.dject.lifecycle;

import org.dxee.dject.internal.AbstractTypeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/**
 * Each LifecycleFeature provides support for specific predestory or AutoCloseable
 * processing of an injected object.
 * <p>
 * {@link PreDestroyLifecycleFeature}s are added via a multibinding. For example,
 *
 * <pre>
 * {@code
 * Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding().to(MyPreDestroyLifecycleFeature.class);
 * }
 * </pre>
 *
 * @author bing.fan
 * 2018-06-28 15:56
 */
public abstract class PreDestroyLifecycleFeature extends AbstractLifecycleFeature {
    @Override
    public PreDestroyTypeVisitor visitor() {
        return new PreDestroyTypeVisitor(this.annotationClazz);
    }

    private class PreDestroyTypeVisitor extends AbstractTypeVisitor {
        private final Logger LOGGER = LoggerFactory.getLogger(PreDestroyTypeVisitor.class);

        public PreDestroyTypeVisitor(Class<? extends Annotation> annotationClazz) {
            super(annotationClazz);
        }

        @Override
        public void addLifecycleAction(LifecycleAction lifecycleAction) {
            lifecycleActions.add(lifecycleAction);
        }

        @Override
        public boolean visit(final Class<?> clazz) {
            boolean continueVisit = !clazz.isInterface();
            if (continueVisit && AutoCloseable.class.isAssignableFrom(clazz)) {
                AutoCloseableLifecycleAction closeableAction = new AutoCloseableLifecycleAction(
                        clazz.asSubclass(AutoCloseable.class));
                LOGGER.debug("adding action {}", closeableAction);
                lifecycleActions.add(closeableAction);
                continueVisit = false;
            }
            return continueVisit;
        }
    }


    @Override
    public String toString() {
        return new StringBuilder().append("Predestroy @").append(this.annotationClazz==null ? "null" : this.annotationClazz.getSimpleName())
                .append(" with priority ").append(priority()).toString();
    }
}
