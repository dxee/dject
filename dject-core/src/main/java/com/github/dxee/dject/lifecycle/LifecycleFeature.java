package com.github.dxee.dject.lifecycle;

import java.util.List;

/**
 * Each LifecycleFeature provides support for specific post constructor 
 * pre destroy processing of an injected object.
 * 
 * @author elandau
 */
public interface LifecycleFeature {
    /**
     * Return a list of actions to perform on object of this type as part of
     * lifecycle processing.  Each LifecycleAction will likely be tied to processing
     * of a specific field or method.
     *
     * @param type
     * @return
     */
    List<LifecycleAction> getActionsForType(Class<?> type);

    /**
     * The priority in a LifecycleFeature List, smaller first.
     * @return
     */
    int priority();
}
