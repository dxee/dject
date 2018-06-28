package org.dxee.dject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.dxee.dject.lifecycle.JSR250Module;
import org.dxee.dject.lifecycle.LifecycleInjector;
import org.dxee.dject.lifecycle.LifecycleInjectorCreator;

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
    
    public static Module asModule(final Object o) {
        return asModule(o);
    }

    public static Module asModule(final Object... instances) {
        return new InstancesModule(instances);
    }

    public static LifecycleInjector inject(final Object... instances) {
        return InjectorBuilder.fromModule(new InstancesModule(instances)).createInjector();
    }

    public static InjectorBuilder fromModules(Module... module) {
        return InjectorBuilder.fromModules(module).combineWith(new JSR250Module());
    }
    
    public TestSupport withSingleton(final Object... instances) {
        module.instances.addAll(Arrays.asList(instances));
        return this;       
    }
    
    public LifecycleInjector inject() {
        return new LifecycleInjectorCreator().createInjector(Stage.PRODUCTION, module);
    }

}
