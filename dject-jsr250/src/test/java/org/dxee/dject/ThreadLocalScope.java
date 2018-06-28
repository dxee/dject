package org.dxee.dject;

import com.google.common.collect.Maps;
import com.google.inject.*;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public class ThreadLocalScope implements Scope {

    private final ThreadLocal<Map<Key<?>, Object>> content = new ThreadLocal<Map<Key<?>, Object>>();

    public void enter() {
        checkState(content.get() == null, "ThreadLocalScope already exists in thread " + Thread.currentThread().getId());
        content.set(Maps.<Key<?>, Object> newHashMap());
    }

    public void exit() {
        checkState(content.get() != null, "No ThreadLocalScope found in thread " + Thread.currentThread().getId());
        content.remove();
    }
    
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {
            public T get() {
                Map<Key<?>, Object> scopedObjects = content.get();
                if (scopedObjects == null) {
                    throw new OutOfScopeException("No ThreadLocalScope found in thread " + Thread.currentThread().getId());
                }

                @SuppressWarnings("unchecked")
                T current = (T) scopedObjects.get(key);
                if (current == null && !scopedObjects.containsKey(key)) {
                    current = unscoped.get();

                    // don't remember proxies; these exist only to serve
                    // circular dependencies
                    if (Scopes.isCircularProxy(current)) {
                        return current;
                    }

                    scopedObjects.put(key, current);
                }
                return current;
            }
        };
    }
}