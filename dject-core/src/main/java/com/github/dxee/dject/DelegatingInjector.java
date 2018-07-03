package com.github.dxee.dject;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.*;
import com.google.inject.spi.TypeConverterBinding;

abstract class DelegatingInjector implements Injector {
    private Injector injector;

    public DelegatingInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void injectMembers(Object instance) {
        injector.injectMembers(instance);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        return injector.getMembersInjector(typeLiteral);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return injector.getMembersInjector(type);
    }

    @Override
    public Map<Key<?>, Binding<?>> getBindings() {
        return injector.getBindings();
    }

    @Override
    public Map<Key<?>, Binding<?>> getAllBindings() {
        return injector.getAllBindings();
    }

    @Override
    public <T> Binding<T> getBinding(Key<T> key) {
        return injector.getBinding(key);
    }

    @Override
    public <T> Binding<T> getBinding(Class<T> type) {
        return injector.getBinding(type);
    }

    @Override
    public <T> Binding<T> getExistingBinding(Key<T> key) {
        return injector.getExistingBinding(key);
    }

    @Override
    public <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> type) {
        return injector.findBindingsByType(type);
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        return injector.getProvider(key);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return injector.getProvider(type);
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        return injector.getInstance(key);
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return injector.getInstance(type);
    }

    @Override
    public Injector getParent() {
        return injector.getParent();
    }

    @Override
    public Injector createChildInjector(Iterable<? extends Module> modules) {
        return injector.createChildInjector(modules);
    }

    @Override
    public Injector createChildInjector(Module... modules) {
        return injector.createChildInjector(modules);
    }

    @Override
    public Map<Class<? extends Annotation>, Scope> getScopeBindings() {
        return injector.getScopeBindings();
    }

    @Override
    public Set<TypeConverterBinding> getTypeConverterBindings() {
        return injector.getTypeConverterBindings();
    }
}
