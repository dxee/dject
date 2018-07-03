package com.github.dxee.dject;

import com.google.inject.AbstractModule;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

public class DjectBuilderTest {
    @Test
    public void testBindingTracing() {
        DjectBuilder
                .fromModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(String.class).toInstance("Hello world");
                    }
                })
                .traceEachBinding()
                .forEachElement(new DefaultElementVisitor<String>() {

                })
                .createInjector();
    }

    @Test
    public void testKeyTracing() {
        try (Dject li = DjectBuilder
                .fromModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(String.class).toInstance("Hello world");
                    }
                })
                .warnOfToInstanceInjections()
                .traceEachKey()
                .createInjector()) {
        }
    }

    @Test
    public void testWarnOnStaticInjection() {
        List<Element> elements = DjectBuilder
                .fromModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        this.requestStaticInjection(String.class);
                    }
                })
                .warnOfStaticInjections()
                .getElements();

        Assert.assertEquals(1, elements.size());
    }

    @Test
    public void testStripStaticInjection() {
        List<Element> elements = DjectBuilder
                .fromModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        this.requestStaticInjection(String.class);
                    }
                })
                .stripStaticInjections()
                .warnOfStaticInjections()
                .getElements();

        Assert.assertEquals(0, elements.size());
    }

    public static class ModuleA extends AbstractModule {
        @Override
        protected void configure() {
            install(new ModuleB());
            install(new ModuleC());
        }
    }

    public static class ModuleB extends AbstractModule {
        @Override
        protected void configure() {
            install(new ModuleC());
        }
    }

    public static class ModuleC extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).toInstance("Hello world");
        }

        @Override
        public int hashCode() {
            return ModuleC.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(getClass());
        }
    }

    @Test
    public void testTraceModules() {
        DjectBuilder
                .fromModule(new ModuleA())
                .traceEachModuleSource()
                .createInjector();
    }

    @Before
    public void printTestHeader() {
        System.out.println("\n=======================================================");
        System.out.println("  Running Test : " + name.getMethodName());
        System.out.println("=======================================================\n");
    }

    @Rule
    public TestName name = new TestName();
}