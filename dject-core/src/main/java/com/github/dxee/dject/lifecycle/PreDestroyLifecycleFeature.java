package com.github.dxee.dject.lifecycle;

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
public interface PreDestroyLifecycleFeature extends LifecycleFeature {
}
