package org.dxee.dject.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * This module add JSR250 lifecycle support.
 * @author bing.fan
 * 2018-06-11 11:57
 */
public class JSR250Module extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().to(JSR250PostConstructLifecycleFeature.class);
        Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding().to(JSR250PreDestroyLifecycleFeature.class);
    }
}
