package com.github.dxee.dject.lifecycle;

import com.github.dxee.dject.lifecycle.impl.SimpleLifecycleShutdown;
import com.google.inject.ImplementedBy;

/**
 * Shutdown for the lifecycle manager.  Code can either block on the shutdown
 * being fired or trigger it from a shutdown mechanism, such as a shutdown PID or
 * shutdown socket.  Each container is likely to have it's own implementation of
 * shutdown shutdown.
 * 
 * @author elandau
 *
 */
@ImplementedBy(SimpleLifecycleShutdown.class)
public interface LifecycleShutdown {
    /**
     * Signal shutdown
     */
    void shutdown();
    
    /**
     * Wait for shutdown to be signalled.  This could be either the result of 
     * calling shutdown() or an internal shutdown mechanism for the container.
     * 
     * @throws InterruptedException
     */
    void awaitShutdown() throws InterruptedException;

}
