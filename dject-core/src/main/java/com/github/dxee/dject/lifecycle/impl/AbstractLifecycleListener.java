package com.github.dxee.dject.lifecycle.impl;


import com.github.dxee.dject.lifecycle.LifecycleListener;

public abstract class AbstractLifecycleListener implements LifecycleListener {
    @Override
    public void onStopped(Throwable t) {
    }

    @Override
    public void onStarted() {
    }
}
