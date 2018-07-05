package com.github.dxee.dject.visitors;

import com.google.inject.Binding;
import com.google.inject.spi.DefaultElementVisitor;

/**
 * Visitor for logging the entire binding information for each Element
 */
public class BindingTracingVisitor extends DefaultElementVisitor<String> {
    @Override
    public <T> String visit(Binding<T> binding) {
        return binding.toString();
    }
}
