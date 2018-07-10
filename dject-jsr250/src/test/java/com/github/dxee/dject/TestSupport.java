package com.github.dxee.dject;

import com.github.dxee.dject.lifecycle.JSR250Module;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSupport {
    private static final class InstancesModule extends AbstractModule {
        final List<Object> instances;

        public InstancesModule(Object... instances) {
            this.instances = new ArrayList<>(Arrays.asList(instances));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected void configure() {
            install(new JSR250Module());
            for (Object o : instances) {
                Class clz = (Class) o.getClass();
                bind(clz).toInstance(o);
            }
        }
    }

    private InstancesModule module = new InstancesModule();
    
    public TestSupport withSingleton(final Object... instances) {
        module.instances.addAll(Arrays.asList(instances));
        return this;       
    }
    
    public Dject inject() {
        return Dject.builder().withModule(module).withStage(Stage.PRODUCTION).build();
    }

    public static Dject inject(final Object... instances) {
        return Dject.builder().withModule(new InstancesModule(instances)).build();
    }

    public static Module asModule(final Object o) {
        return asModule(o);
    }

    public static Module asModule(final Object... instances) {
        return new InstancesModule(instances);
    }

    public static Dject.Builder fromModules(Module... module) {
        return Dject.builder().withModules(module).withCombineModules(new JSR250Module());
    }
}
