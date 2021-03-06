package com.github.dxee.dject.lifecycle;

import java.lang.ref.Reference;
import java.util.concurrent.Callable;

/**
 * Runnable that applies one or more LifecycleActions to a managed instance T or Reference&lt;T&gt;.
 * For Reference&lt;T&gt; the action is invoked on a best-effort basis, if the referent is non-null
 * at time run() is invoked
 */
public final class ManagedInstanceAction implements Callable<Void> {
    private final Object target; // the managed instance
    private final Reference<?> targetReference; // reference to the managed instance
    private final Iterable<LifecycleAction> actions; // set of actions that will be applied to target

    public ManagedInstanceAction(Object target, Iterable<LifecycleAction> actions) {
        // keep hard reference to target
        this.target = target;
        this.targetReference = null;
        this.actions = actions;
    }

    public ManagedInstanceAction(Reference<?> target, Object context, Iterable<LifecycleAction> actions) {
        this.target = null;
        this.targetReference = target; // keep hard reference to target
        this.actions = actions;
    }

    @Override
    public Void call() throws Exception {
        Object target = (targetReference == null) ? this.target : targetReference.get();
        if (target != null) {
            for (LifecycleAction m : actions) {
                m.call(target);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ManagedInstanceAction{"
                + "target=" + target
                + ", targetReference=" + targetReference
                + ", actions=" + actions
                + '}';
    }
}