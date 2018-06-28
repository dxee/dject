package org.dxee.dject.lifecycle;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;

/**
 * Special LifecycleFeature to support @PostConstruct annotation processing.
 * @author bing.fan
 * 2018-06-07 19:54
 */
public final class JSR250PostConstructLifecycleFeature extends PostConstructLifecycleFeature {
    @Override
    public Class<? extends Annotation> annotationClazz() {
        return PostConstruct.class;
    }

    @Override
    public int priority() {
        return 1;
    }
}
