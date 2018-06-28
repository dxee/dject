package org.dxee.dject.lifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CountDownLatch;

/**
 * Default shutdown signal, mostly to be used for runtime applications, using
 * a CountDown latch.
 * 
 * @author elandau
 *
 */
@Singleton
public class DefaultLifecycleShutdownSignal extends AbstractLifecycleShutdownSignal {
    @Inject
    public DefaultLifecycleShutdownSignal(LifecycleManager manager) {
        super(manager);
    }

    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void signal() {
        shutdown();
        latch.countDown();
    }

    @Override
    public void await() throws InterruptedException {
        latch.await();
    }
}
