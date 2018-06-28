package org.dxee.dject.lifecycle;

import org.dxee.dject.internal.AbstractTypeVisitor;

import java.lang.annotation.Annotation;

/**
 * Each LifecycleFeature provides support for specific post constructor
 * processing of an injected object.
 *
 * {@link PostConstructLifecycleFeature}s are added via a multibinding. For example,
 *
 * <pre>
 * {@code
 * Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().to(JSR250PostConstructLifecycleFeature.class);
 * }
 * </pre>
 *
 * @author bing.fan
 * 2018-06-28 15:56
 */
public abstract class PostConstructLifecycleFeature extends AbstractLifecycleFeature {

    @Override
    public PostConstructTypeVisitor visitor() {
        return new PostConstructTypeVisitor(this.annotationClazz);
    }

    private class PostConstructTypeVisitor extends AbstractTypeVisitor {
        public PostConstructTypeVisitor(Class<? extends Annotation> annotationClazz) {
            super(annotationClazz);
        }

        @Override
        public void addLifecycleAction(LifecycleAction lifecycleAction) {
            lifecycleActions.addFirst(lifecycleAction);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder().append("PostConstruct @").append(this.annotationClazz==null ? "null" : this.annotationClazz.getSimpleName())
                .append(" with priority ").append(priority()).toString();
    }
}
