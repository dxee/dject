package com.github.dxee.dject.lifecycle.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import com.github.dxee.dject.lifecycle.LifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Wrapper for any LifecycleListener to provide this following functionality
 * 1.  Logging of events as INFO
 * 2.  Swallow any event handler exceptions during shutdown
 */
public final class SafeLifecycleListener extends WeakReference<LifecycleListener> implements LifecycleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeLifecycleListener.class);
    private final int delegateHash;
    private final String asString;

    public static SafeLifecycleListener wrap(LifecycleListener listener) {
        Preconditions.checkNotNull(listener, "listener argument must be non-null");
        return new SafeLifecycleListener(listener, null);
    }

    public static SafeLifecycleListener wrap(LifecycleListener listener, ReferenceQueue<LifecycleListener> refQueue) {
        Preconditions.checkNotNull(listener, "listener argument must be non-null");
        return new SafeLifecycleListener(listener, refQueue);
    }

    private SafeLifecycleListener(LifecycleListener delegate, ReferenceQueue<LifecycleListener> refQueue) {
        super(delegate, refQueue);
        this.delegateHash = delegate.hashCode();
        this.asString = "SafeLifecycleListener@" + System.identityHashCode(this) + " [" + delegate.toString() + "]";
    }

    @Override
    public void onStarted() {
        LifecycleListener delegate = get();
        if (delegate != null) {
            LOGGER.info("Starting '{}'", delegate);
            delegate.onStarted();
        }
    }

    @Override
    public void onStopped(Throwable t) {
        LifecycleListener delegate = get();
        if (delegate != null) {
            if (t != null) {
                LOGGER.info("Stopping '{}' due to '{}@{}'", delegate,
                        t.getClass().getSimpleName(),
                        System.identityHashCode(t));
            } else {
                LOGGER.info("Stopping '{}'", delegate);
            }
            try {
                delegate.onStopped(t);
            } catch (Exception e) {
                LOGGER.info("onStopped failed for {}", delegate, e);
            }
        }
    }

    @Override
    public String toString() {
        return asString;
    }

    @Override
    public int hashCode() {
        return delegateHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LifecycleListener delegate = get();
        if (delegate != null) {
            LifecycleListener otherDelegate = ((SafeLifecycleListener) obj).get();
            return delegate == otherDelegate || delegate.equals(otherDelegate);
        } else {
            return false;
        }
    }

}
