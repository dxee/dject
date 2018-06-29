package org.dxee.dject.lifecycle;

import org.dxee.dject.lifecycle.impl.AbstractTypeVisitor;
import org.dxee.dject.lifecycle.impl.OneAnnotationLifecycleFeature;

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
public interface PostConstructLifecycleFeature extends LifecycleFeature {
}
