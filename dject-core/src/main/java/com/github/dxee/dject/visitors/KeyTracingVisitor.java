package com.github.dxee.dject.visitors;

import com.google.inject.Binding;
import com.google.inject.spi.DefaultElementVisitor;

/**
 * Visitor for logging only the Key for each {@code Element} binding
 */
public class KeyTracingVisitor extends DefaultElementVisitor<String> {
    @Override
    public <T> String visit(Binding<T> binding) {
        return binding.getKey().toString();
    }
}
