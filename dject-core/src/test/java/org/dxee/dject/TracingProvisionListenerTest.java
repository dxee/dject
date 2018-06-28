package org.dxee.dject;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import org.dxee.dject.lifecycle.LifecycleInjector;
import org.dxee.dject.trace.TracingProvisionListener;
import org.junit.Test;

public class TracingProvisionListenerTest {
    @Test
    public void testDefault() {
        LifecycleInjector injector = InjectorBuilder
            .fromModules(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bindListener(Matchers.any(), TracingProvisionListener.createDefault());
                    }
                })
            .createInjector();
    }
}
