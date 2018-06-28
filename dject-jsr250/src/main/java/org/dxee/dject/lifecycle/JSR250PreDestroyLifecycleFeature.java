package org.dxee.dject.lifecycle;

import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;

/**
 * Special LifecycleFeature to support @PreDestroy annotation processing and
 * java.lang.AutoCloseable detection.
 * @author bing.fan
 * 2018-06-08 20:03
 */
public final class JSR250PreDestroyLifecycleFeature extends PreDestroyLifecycleFeature {
    @Override
    public Class<? extends Annotation> annotationClazz() {
        return PreDestroy.class;
    }

    @Override
    public int priority() {
        return 1;
    }
}
