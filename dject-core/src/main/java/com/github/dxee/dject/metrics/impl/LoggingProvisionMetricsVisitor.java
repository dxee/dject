package com.github.dxee.dject.metrics.impl;

import com.github.dxee.dject.metrics.ProvisionMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LoggingProvisionMetricsVisitor implements ProvisionMetrics.Visitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingProvisionMetricsVisitor.class);

    int level = 1;
    int elementCount = 0;
    
    @Override
    public void visit(ProvisionMetrics.Element entry) {
        elementCount++;
        LOGGER.info(String.format("%" + (level * 3 - 2) + "s%s%s : %d ms (%d ms)",
                "",
                entry.getKey().getTypeLiteral().toString(), 
                entry.getKey().getAnnotation() == null ? "" : " [" + entry.getKey().getAnnotation() + "]",
                entry.getTotalDuration(TimeUnit.MILLISECONDS),
                entry.getDuration(TimeUnit.MILLISECONDS)
                ));
        level++;
        entry.accept(this);
        level--;
    }
    
    int getElementCount() {
        return elementCount;
    }
}
