package com.github.dxee.dject.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AutoCloseableLifecycleAction
 * @author bing.fan
 * 2018-06-07 19:46
 */
public class AutoCloseableLifecycleAction implements LifecycleAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoCloseableLifecycleAction.class);

    private final String description;

    public AutoCloseableLifecycleAction(Class<? extends AutoCloseable> clazz) {
        this.description = new StringBuilder().append("AutoCloseable@")
                .append(System.identityHashCode(this))
                .append("[")
                .append(clazz.getName()).append(".")
                .append("close()")
                .append("]").toString();
    }

    @Override
    public void call(Object obj) throws Exception {
        LOGGER.info("calling action {}", description);
        AutoCloseable.class.cast(obj).close();
    }

    @Override
    public String toString() {
        return description;
    }
}
