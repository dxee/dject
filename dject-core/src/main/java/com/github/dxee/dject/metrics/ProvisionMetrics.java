package com.github.dxee.dject.metrics;

import java.util.concurrent.TimeUnit;

import com.google.inject.ImplementedBy;
import com.google.inject.Key;

/**
 * Interface invoked by LifecycleModule's ProvisionListener to
 * gather metrics on objects as they are provisioned.  Through the
 * metrics listener it's possible to generate a dependency tree
 * for the first initialization of all objects.  Note that no call
 * will be made for singletons that are being injected but have
 * already been instantiated.
 */
@ImplementedBy(SimpleProvisionMetrics.class)
public interface ProvisionMetrics {

    /**
     * Node used to track metrics for an object that has been provisioned
     */
    interface Element {
        Key<?> getKey();

        long getDuration(TimeUnit units);

        long getTotalDuration(TimeUnit units);

        void accept(Visitor visitor);
    }

    /**
     * Visitor API for traversing nodes
     */
    interface Visitor {
        void visit(Element element);
    }

    /**
     * Notification that an object of type 'key' is about to be created.
     * Note that there will likely be several nested calls to push() as
     * dependencies are injected.
     *
     * @param key
     */
    void push(Key<?> key);

    /**
     * Pop and finalize initialization of the latest object to be provisioned.
     * A matching pop will be called for each push().
     */
    void pop();

    /**
     * Traverse the elements using the visitor pattern.
     *
     * @param visitor
     */
    void accept(Visitor visitor);
}
