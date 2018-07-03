package com.github.dxee.dject.event.guava;

import com.github.dxee.dject.event.ApplicationEventRegistration;
import com.google.common.eventbus.EventBus;

class GuavaEventRegistration implements ApplicationEventRegistration {

    private final EventBus eventBus;
    private final GuavaSubscriberProxy subscriber;

    public GuavaEventRegistration(EventBus eventBus, GuavaSubscriberProxy subscriber) {
        this.eventBus = eventBus;
        this.subscriber = subscriber;
    }

    public void unregister() {
        this.eventBus.unregister(subscriber);
    }
}
