package org.dxee.dject.lifecycle;

import org.dxee.dject.internal.AbstractTypeVisitor;
import org.dxee.dject.internal.TypeInspector;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Supplier;

/**
 * AbstractLifecycleFeature
 * @author bing.fan
 * 2018-06-08 20:15
 */
abstract class AbstractLifecycleFeature implements LifecycleFeature {
    protected Class<? extends Annotation> annotationClazz;

    @Override
    public List<LifecycleAction> getActionsForType(final Class<?> type) {
        if(null == annotationClazz) {
            annotationClazz = annotationClazz();
        }
        return TypeInspector.accept(type, visitor());
    }

    abstract public <V extends Supplier<List<LifecycleAction>> & TypeInspector.TypeVisitor> V visitor();

    @Override
    public int priority() {
        return 0;
    }
}
