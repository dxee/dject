package com.github.dxee.dject;

import com.github.dxee.dject.lifecycle.*;
import com.github.dxee.dject.metrics.ProvisionMetricsModule;
import com.github.dxee.dject.metrics.impl.LoggingProvisionModule;
import com.github.dxee.dject.trace.TracingProvisionListener;
import com.github.dxee.dject.visitors.*;
import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.Elements;
import com.google.inject.util.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;

/**
 * Wrapper for Guice's Injector with extended methods.
 */
@Singleton
public final class Dject extends DelegatingInjector {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dject.class);
    private final LifecycleManager manager = new LifecycleManager();
    private final Stage stage;
    private final Module module;

    // From guice
    @Inject
    private LifecycleShutdown lifecycleShutdown;


    public Dject(Builder builder) {
        this.stage = builder.stage;
        this.module = builder.module;

        // create guice injector here
        this.injector = createInjector();
        this.injector.injectMembers(this);
    }

    /**
     * Create the injector
     * @return Injector
     */
    private Injector createInjector() {
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
                            bind(LifecycleManager.class).toInstance(manager);
                        }
                    },
                    module
            );
            manager.notifyStarted();
            LOGGER.info("Injector created successfully ");
            return injector;
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

    /**
     * Shutdown for the lifecycle manager
     */
    public void shutdown() {
        lifecycleShutdown.shutdown();
    }

    /**
     * Block until lifecycle manager shutdown
     *
     * @throws InterruptedException
     */
    public void awaitShutdown() throws InterruptedException {
        lifecycleShutdown.awaitShutdown();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Stage stage = Stage.DEVELOPMENT;
        private Module module;

        public Builder withStage(Stage stage) {
            this.stage = stage;
            return this;
        }

        public Builder withModule(Module module) {
            this.module = module;
            return this;
        }

        public Builder withModules(Module... modules) {
            this.module = Modules.combine(modules);
            return this;
        }

        public Builder withModules(List<Module> modules) {
            this.module = Modules.combine(modules);
            return this;
        }

        public Builder withOverrideModules(Module... modules) {
            this.module = Modules.override(module).with(modules);
            return this;
        }

        public Builder withOverrideModules(Collection<Module> modules) {
            this.module = Modules.override(module).with(modules);
            return this;
        }

        public Builder withCombineModules(Module... modules) {
            List<Module> m = new ArrayList<>();
            m.add(module);
            m.addAll(Arrays.asList(modules));
            this.module = Modules.combine(m);
            return this;
        }

        /**
         * For debug purpose, See {@link LoggingProvisionModule}
         */
        public Builder withLoggingProvision() {
            withCombineModules(new LoggingProvisionModule());
            return this;
        }

        /**
         * For debug purpose, See {@link TracingProvisionListener}
         */
        public Builder withTracingProvision() {
            withCombineModules(new AbstractModule() {
                @Override
                protected void configure() {
                    bindListener(Matchers.any(), TracingProvisionListener.createDefault());
                }
            });
            return this;
        }

        /**
         * Call the provided visitor for all elements of the current module.
         * <p>
         * This call will not modify any bindings
         *
         * @param visitor visitor
         */
        public <T> Builder withEachElementVister(ElementVisitor<T> visitor) {
            Elements.getElements(module)
                    .forEach(element -> element.acceptVisitor(visitor));
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
        public <T> Builder withEachElementVister(ElementVisitor<T> visitor, Consumer<T> consumer) {
            Elements.getElements(module).forEach(
                    element -> Optional.ofNullable(element.acceptVisitor(visitor)).ifPresent(consumer)
            );
            return this;
        }

        /**
         * Log the current binding state.  traceEachKey() is useful for debugging a sequence of
         * operation where the binding snapshot can be dumped to the log after an operation.
         */
        public Builder withTraceEachKey() {
            return withEachElementVister(new KeyTracingVisitor(), message -> LOGGER.debug(message));
        }

        /**
         * Log each binding
         */
        public Builder withTraceEachBinding() {
            return withEachElementVister(new BindingTracingVisitor(), message -> LOGGER.debug(message));
        }

        /**
         * Log  each modulesource
         */
        public Builder withTraceEachModuleSource() {
            return withEachElementVister(new ModuleSourceTracingVisitor(), message -> LOGGER.debug(message));
        }

        /**
         * Log each provision listener
         */
        public Builder withTraceEachProvisionListener() {
            return withEachElementVister(new ProvisionListenerTracingVisitor(), message -> LOGGER.debug(message));
        }

        /**
         * Log a warning that static injection is being used.  Static injection is considered a 'hack'
         * to alllow for backwards compatibility with non DI'd static code.
         */
        public Builder withWarnOfStaticInjections() {
            return withEachElementVister(new WarnOfStaticInjectionVisitor(), message -> LOGGER.debug(message));
        }

        /**
         * Log a warning that instance injection is being used.
         */
        public Builder withWarnOfToInstanceInjections() {
            return withEachElementVister(new WarnOfToInstanceInjectionVisitor(), message -> LOGGER.debug(message));
        }

        /**
         * Filter out elements for which the provided visitor returns true.
         *
         * @param predicate predicate
         */
        public Builder withFilter(ElementVisitor<Boolean> predicate) {
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
        public Builder withStripStaticInjections() {
            return withFilter(new IsNotStaticInjectionVisitor());
        }

        /**
         * @return Return all elements in the managed module
         */
        public List<Element> getElements() {
            return Elements.getElements(Stage.TOOL, module);
        }

        public Dject build() {
            return new Dject(this);
        }
    }
}
