package org.dxee.dject.visitors;

import com.google.inject.Binding;
import com.google.inject.spi.DefaultElementVisitor;
import org.dxee.dject.DjectBuilder;

/**
 * Visitor for logging the entire binding information for each Element
 * 
 * To use with {@link DjectBuilder},
 * 
 * <code>
 * DjectBuilder
 *      .withModules(new MyApplicationModule)
 *      .forEachElement(new BindingTracingVisitor())
 *      .createInjector();
 * </code>
 */
public class BindingTracingVisitor extends DefaultElementVisitor<String> {
    @Override
    public <T> String visit(Binding<T> binding) {
        return binding.toString();
    }
}
