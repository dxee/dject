package com.github.dxee.dject.lifecycle;

/**
 * Generic interface for actions to be invoked as part of lifecycle 
 * management.  This includes actions such as PostConstruct, PreDestroy
 * and configuration related mapping.
 * 
 * @see LifecycleFeature
 * 
 * @author elandau
 *
 */
public interface LifecycleAction {
    /**
     * Invoke the action on an object.
     * 
     * @param obj
     * @throws Exception
     */
    void call(Object obj) throws Exception;
}
