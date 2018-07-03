package com.github.dxee.dject.grapher;

import com.google.inject.AbstractModule;

public final class GrapherModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Grapher.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "GrapherModule[]";
    }

}
