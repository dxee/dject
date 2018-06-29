package org.dxee.dject.metrics;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.dxee.dject.lifecycle.impl.AbstractLifecycleListener;
import org.dxee.dject.lifecycle.LifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoggingProvisionMetricsLifecycleListener extends AbstractLifecycleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingProvisionMetricsLifecycleListener.class);
    
    private final ProvisionMetrics metrics;

    @Inject
    LoggingProvisionMetricsLifecycleListener(LifecycleManager manager, ProvisionMetrics metrics) {
        this.metrics = metrics;
        manager.addListener(this);
    }
    
    @Override
    public void onStarted() {
        LOGGER.info("Injection metrics report as follows:" );
        metrics.accept(new LoggingProvisionMetricsVisitor());
    }
    
    @Override
    public void onStopped(Throwable t) {
        if (t != null) {
            LOGGER.info("Injection metrics report for failed start : \n" );
            metrics.accept(new LoggingProvisionMetricsVisitor());
        }
    }
}
