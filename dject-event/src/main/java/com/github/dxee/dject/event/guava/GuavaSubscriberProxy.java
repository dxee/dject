package com.github.dxee.dject.event.guava;

import com.github.dxee.dject.event.ApplicationEvent;
import com.google.common.eventbus.Subscribe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class GuavaSubscriberProxy {

    private final Object handlerInstance;
    private final Method handlerMethod;
    private final Class<?> acceptedType;

    public GuavaSubscriberProxy(Object handlerInstance, Method handlerMethod, Class<?> acceptedType) {
        this.handlerInstance = handlerInstance;
        this.handlerMethod = handlerMethod;
        this.acceptedType = acceptedType;
    }

    @Subscribe
    public void invokeEventHandler(ApplicationEvent event)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (acceptedType.isAssignableFrom(event.getClass())) {
            if (!handlerMethod.isAccessible()) {
                handlerMethod.setAccessible(true);
            }
            handlerMethod.invoke(handlerInstance, event);
        }
    }
}
