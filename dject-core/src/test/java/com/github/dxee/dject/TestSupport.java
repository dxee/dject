package com.github.dxee.dject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

import com.github.dxee.dject.feature.DjectFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Stage;

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

    private InstancesModule module = new InstancesModule();
    private IdentityHashMap<DjectFeature<?>, Object> features = new IdentityHashMap<>();

    public <T> TestSupport withFeature(DjectFeature<T> feature, T value) {
        this.features.put(feature, value);
        return this;
    }

    public Dject inject() {
        return Dject.builder().withFeatures(features)
                .withStage(Stage.PRODUCTION).withModules(module).build();
    }

    public static Dject inject(final Object... instances) {
        return Dject.builder().withModule(new InstancesModule(instances)).build();
    }

    public TestSupport withSingleton(final Object... instances) {
        module.instances.addAll(Arrays.asList(instances));
        return this;
    }
}
