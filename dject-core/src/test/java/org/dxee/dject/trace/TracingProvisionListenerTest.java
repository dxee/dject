package org.dxee.dject.trace;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import org.dxee.dject.Dject;
import org.dxee.dject.DjectBuilder;
import org.dxee.dject.trace.TracingProvisionListener;
import org.junit.Test;

public class TracingProvisionListenerTest {
    @Test
    public void testDefault() {
        Dject injector = DjectBuilder
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