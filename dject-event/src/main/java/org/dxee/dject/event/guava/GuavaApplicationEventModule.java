package org.dxee.dject.event.guava;

import com.google.inject.AbstractModule;
import org.dxee.dject.event.ApplicationEventDispatcher;
import org.dxee.dject.event.ApplicationEventModule;

public final class GuavaApplicationEventModule extends AbstractModule {   
     
    @Override
    protected void configure() {  
        install(new ApplicationEventModule());
        bind(ApplicationEventDispatcher.class).to(GuavaApplicationEventDispatcher.class).asEagerSingleton();
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
        return "GuavaApplicationEventModule[]";
    }

}
