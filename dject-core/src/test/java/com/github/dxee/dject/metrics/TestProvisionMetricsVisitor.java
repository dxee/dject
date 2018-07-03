package com.github.dxee.dject.metrics;

public class TestProvisionMetricsVisitor implements ProvisionMetrics.Visitor {
    int level = 1;
    int elementCount = 0;
    
    @Override
    public void visit(ProvisionMetrics.Element entry) {
        elementCount++;
        level++;
        entry.accept(this);
        level--;
    }
    
    int getElementCount() {
        return elementCount;
    }
}
