package com.github.dxee.dject.trace;

import com.github.dxee.dject.Dject;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import org.junit.Test;

public class TracingProvisionListenerTest {
    @Test
    public void testDefault() {
       Dject.newBuilder().withModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bindListener(Matchers.any(), TracingProvisionListener.createDefault());
                    }
                })
            .build();
    }
}
