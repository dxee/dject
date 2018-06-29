package org.dxee.dject.lifecycle.impl;

import org.dxee.dject.lifecycle.LifecycleListener;

public abstract class AbstractLifecycleListener implements LifecycleListener {
    @Override
    public void onStopped(Throwable t) {
    }

    @Override
    public void onStarted() {
    }
}
