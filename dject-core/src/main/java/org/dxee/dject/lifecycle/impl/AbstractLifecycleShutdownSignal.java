package org.dxee.dject.lifecycle.impl;

import org.dxee.dject.lifecycle.LifecycleManager;
import org.dxee.dject.lifecycle.LifecycleShutdownSignal;

public abstract class AbstractLifecycleShutdownSignal implements LifecycleShutdownSignal {

    private final LifecycleManager manager;

    protected AbstractLifecycleShutdownSignal(LifecycleManager manager) {
        this.manager = manager;
    }
    
    protected void shutdown() {
        manager.notifyShutdown();
    }
}
