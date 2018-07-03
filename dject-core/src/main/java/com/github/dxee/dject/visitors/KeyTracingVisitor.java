package com.github.dxee.dject.visitors;

import com.github.dxee.dject.DjectBuilder;
import com.google.inject.Binding;
import com.google.inject.spi.DefaultElementVisitor;

/**
 * Visitor for logging only the Key for each {@code Element} binding
 * 
 * To use with {@link DjectBuilder}
 * 
 * <code>
 * DjectBuilder
 *      .fromModule(new MyApplicationModule)
 *      .forEachElement(new KeyTracingVisitor())
 *      .createInjector();
 * </code>
 */
public class KeyTracingVisitor extends DefaultElementVisitor<String> {
    @Override
    public <T> String visit(Binding<T> binding) {
        return binding.getKey().toString();
    }
}
