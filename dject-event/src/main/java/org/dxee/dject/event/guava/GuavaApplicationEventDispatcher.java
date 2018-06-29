package org.dxee.dject.event.guava;

import com.google.common.eventbus.EventBus;
import com.google.common.reflect.TypeToken;
import org.dxee.dject.event.ApplicationEvent;
import org.dxee.dject.event.ApplicationEventDispatcher;
import org.dxee.dject.event.ApplicationEventListener;
import org.dxee.dject.event.ApplicationEventRegistration;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class GuavaApplicationEventDispatcher implements ApplicationEventDispatcher {

    private final EventBus eventBus;
    private final Method eventListenerMethod;

    @Inject
    public GuavaApplicationEventDispatcher(EventBus eventBus) {
        this.eventBus = eventBus;
        try {
            this.eventListenerMethod = ApplicationEventListener.class.getDeclaredMethod("onEvent", ApplicationEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to cache ApplicationEventListener method", e);
        }
    }

    public ApplicationEventRegistration registerListener(Object instance, Method method, Class<? extends ApplicationEvent> eventType) {
        GuavaSubscriberProxy proxy = new GuavaSubscriberProxy(instance, method, eventType);
        eventBus.register(proxy);
        return new GuavaEventRegistration(eventBus, proxy);
    }

    public <T extends ApplicationEvent> ApplicationEventRegistration registerListener(Class<T> eventType, ApplicationEventListener<T> eventListener) {
        GuavaSubscriberProxy proxy = new GuavaSubscriberProxy(eventListener, eventListenerMethod, eventType);
        eventBus.register(proxy);
        return new GuavaEventRegistration(eventBus, proxy);
    }

    public ApplicationEventRegistration registerListener(ApplicationEventListener<? extends ApplicationEvent> eventListener) {
        Type[] genericInterfaces = eventListener.getClass().getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (ApplicationEventListener.class.isAssignableFrom(TypeToken.of(type).getRawType())) {
                ParameterizedType ptype = (ParameterizedType) type;
                Class<?> rawType = TypeToken.of(ptype.getActualTypeArguments()[0]).getRawType();
                GuavaSubscriberProxy proxy = new GuavaSubscriberProxy(eventListener, eventListenerMethod, rawType);
                eventBus.register(proxy);
                return new GuavaEventRegistration(eventBus, proxy);
            }
        }
        return new ApplicationEventRegistration() {
            public void unregister() {}  //no-op. Could not find anything to register.
        };
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        this.eventBus.post(event);
    }
}
