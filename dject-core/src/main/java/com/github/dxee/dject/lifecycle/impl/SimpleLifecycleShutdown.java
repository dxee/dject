package com.github.dxee.dject.lifecycle.impl;

import com.github.dxee.dject.lifecycle.LifecycleManager;
import com.github.dxee.dject.lifecycle.LifecycleShutdown;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CountDownLatch;

/**
 * Default shutdown shutdown, mostly to be used for runtime applications, using
 * a CountDown latch.
 * 
 * @author elandau
 *
 */
@Singleton
public class SimpleLifecycleShutdown implements LifecycleShutdown {
    private final CountDownLatch latch = new CountDownLatch(1);
    private final LifecycleManager manager;

    @Inject
    public SimpleLifecycleShutdown(LifecycleManager manager) {
        this.manager = manager;
    }

    @Override
    public void shutdown() {
        manager.notifyShutdown();
        latch.countDown();
    }

    @Override
    public void awaitShutdown() throws InterruptedException {
        latch.await();
    }
}
