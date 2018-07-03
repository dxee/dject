package org.dxee.dject;

import com.google.inject.*;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.Elements;
import com.google.inject.util.Modules;
import org.dxee.dject.lifecycle.LifecycleListenerModule;
import org.dxee.dject.lifecycle.LifecycleManager;
import org.dxee.dject.lifecycle.LifecycleModule;
import org.dxee.dject.metrics.ProvisionMetricsModule;
import org.dxee.dject.visitors.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Simple DSL on top of Guice through which an injector may be created using a series
 * of operations and transformations of Guice modules.  Operations are tracked using a
 * single module and are additive such that each operation executes on top of the entire
 * current binding state.  Once all bindings have been defined the injector can be created
 * using an {@link Dject} strategy.
 *
 * <code>
 * DjectBuilder
 * .fromModule(new MyApplicationModule())
 * .overrideWith(new OverridesForTesting())
 * .forEachElement(new BindingTracingVisitor())
 * .createInjector();
 * </code>
 */
public final class DjectBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DjectBuilder.class);

    private Module module;

    /**
     * Start the builder using the specified module.
     *
     * @param module Custom modules
     * @return
     */
    public static DjectBuilder fromModule(Module module) {
        return new DjectBuilder(module);
    }

    public static DjectBuilder fromModules(Module... additionalModules) {
        return new DjectBuilder(Modules.combine(additionalModules));
    }

    public static DjectBuilder fromModules(List<Module> modules) {
        return new DjectBuilder(Modules.combine(modules));
    }

    private DjectBuilder(Module module) {
        this.module = module;
    }

    /**
     * Override all existing bindings with bindings in the provided modules.
     * This method uses Guice's build in {@link Modules#override} and is preferable
     * to using {@link Modules#override}.  The approach here is to attempt to promote
     * the use of {@link Modules#override} as a single top level override.  Using
     * {@link Modules#override} inside Guice modules can result in duplicate bindings
     * when the same module is installed in multiple placed.
     *
     * @param modules Custom modules
     */
    public DjectBuilder overrideWith(Module... modules) {
        return overrideWith(Arrays.asList(modules));
    }

    /**
     * @see DjectBuilder#overrideWith(Module...)
     */
    public DjectBuilder overrideWith(Collection<Module> modules) {
        this.module = Modules.override(module).with(modules);
        return this;
    }

    /**
     * Add additional bindings to the module tracked by the DSL
     *
     * @param modules Custom modules
     */
    public DjectBuilder combineWith(Module... modules) {
        List<Module> m = new ArrayList<>();
        m.add(module);
        m.addAll(Arrays.asList(modules));
        this.module = Modules.combine(m);
        return this;
    }

    /**
     * Iterate through all elements of the current module and pass the output of the
     * ElementVisitor to the provided consumer.  'null' responses from the visitor are ignored.
     * <p>
     * This call will not modify any bindings
     *
     * @param visitor visitor
     */
    private <T> DjectBuilder forEachElement(ElementVisitor<T> visitor, Consumer<T> consumer) {
        Elements.getElements(module).forEach(
                element -> Optional.ofNullable(element.acceptVisitor(visitor)).ifPresent(consumer)
        );
        return this;
    }

    /**
     * Call the provided visitor for all elements of the current module.
     * <p>
     * This call will not modify any bindings
     *
     * @param visitor visitor
     */
    public <T> DjectBuilder forEachElement(ElementVisitor<T> visitor) {
        Elements.getElements(module)
                .forEach(element -> element.acceptVisitor(visitor));
        return this;
    }

    /**
     * Log the current binding state.  traceEachKey() is useful for debugging a sequence of
     * operation where the binding snapshot can be dumped to the log after an operation.
     */
    public DjectBuilder traceEachKey() {
        return forEachElement(new KeyTracingVisitor(), message -> LOGGER.debug(message));
    }

    /**
     * Log each binding
     */
    public DjectBuilder traceEachBinding() {
        return forEachElement(new BindingTracingVisitor(), message -> LOGGER.debug(message));
    }

    /**
     * Log  each modulesource
     */
    public DjectBuilder traceEachModuleSource() {
        return forEachElement(new ModuleSourceTracingVisitor(), message -> LOGGER.debug(message));
    }

    /**
     * Log each provision listener
     */
    public DjectBuilder traceEachProvisionListener() {
        return forEachElement(new ProvisionListenerTracingVisitor(), message -> LOGGER.debug(message));
    }

    /**
     * Log a warning that static injection is being used.  Static injection is considered a 'hack'
     * to alllow for backwards compatibility with non DI'd static code.
     */
    public DjectBuilder warnOfStaticInjections() {
        return forEachElement(new WarnOfStaticInjectionVisitor(), message -> LOGGER.debug(message));
    }

    /**
     * Log a warning that instance injection is being used.
     */
    public DjectBuilder warnOfToInstanceInjections() {
        return forEachElement(new WarnOfToInstanceInjectionVisitor(), message -> LOGGER.debug(message));
    }

    /**
     * Filter out elements for which the provided visitor returns true.
     *
     * @param predicate predicate
     */
    public DjectBuilder filter(ElementVisitor<Boolean> predicate) {
        List<Element> elements = new ArrayList<Element>();
        for (Element element : Elements.getElements(Stage.TOOL, module)) {
            if (element.acceptVisitor(predicate)) {
                elements.add(element);
            }
        }
        this.module = Elements.getModule(elements);
        return this;
    }

    /**
     * Filter out all bindings using requestStaticInjection
     */
    public DjectBuilder stripStaticInjections() {
        return filter(new IsNotStaticInjectionVisitor());
    }

    /**
     * @return Return all elements in the managed module
     */
    public List<Element> getElements() {
        return Elements.getElements(Stage.TOOL, module);
    }


    /**
     * Create an injector by stage
     *
     * @param stage The stage of injector
     * @return
     */
    public Dject createInjector(Stage stage) {
        return createInjector(stage, module);
    }


    /**
     * Create an DEVELOPMENT stage injector
     *
     * @return
     */
    public Dject createInjector() {
        return createInjector(Stage.DEVELOPMENT);
    }

    /**
     * Create the injector
     * @param stage Guice stage
     * @param module Custom module
     * @return Inject
     */
    private Dject createInjector(Stage stage, Module module) {
        final LifecycleManager manager = new LifecycleManager();
        // Construct the injector using our override structure
        try {
            Injector injector = Guice.createInjector(
                    stage,
                    // This has to be first to make sure @PostConstruct support is added as early
                    // as possible
                    new ProvisionMetricsModule(),
                    new LifecycleModule(),
                    new LifecycleListenerModule(),
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(Dject.class);
                            bind(LifecycleManager.class).toInstance(manager);
                        }
                    },
                    module
            );
            manager.notifyStarted();
            Dject dject = injector.getExistingBinding(Key.get(Dject.class)).getProvider().get();
            LOGGER.info("Injector created successfully ");
            return dject;
        } catch (Exception e) {
            LOGGER.error("Failed to create injector - {}@{}",
                    e.getClass().getSimpleName(),
                    System.identityHashCode(e),
                    e);
            try {
                manager.notifyStartFailed(e);
            } catch (Exception e2) {
                LOGGER.error("Failed to notify injector creation failure", e2);
            }
            throw e;
        }
    }
}
