package com.github.dxee.dject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.AbstractModule;

public class TestSupport {
    private static final class InstancesModule extends AbstractModule {
        final List<Object> instances;

        public InstancesModule(Object... instances) {
            this.instances = new ArrayList<>(Arrays.asList(instances));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected void configure() {
            for (Object o : instances) {
                Class clz = (Class) o.getClass();
                bind(clz).toInstance(o);
            }
        }
    }

    public static Dject inject(final Object... instances) {
        return DjectBuilder.fromModule(new InstancesModule(instances)).createInjector();
    }

}
