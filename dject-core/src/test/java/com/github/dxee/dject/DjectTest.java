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

public class DjectTest {
    @Test
    public void testBindingTracing() {
        Dject.builder().withModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(String.class).toInstance("Hello world");
                    }
                })
                .withTraceEachBinding()
                .withEachElementVister(new DefaultElementVisitor<String>() {

                })
                .build();
    }

    @Test
    public void testKeyTracing() {
        try (Dject li = Dject.builder().withModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(String.class).toInstance("Hello world");
                    }
                })
                .withWarnOfToInstanceInjections()
                .withTraceEachKey()
                .build()) {
        }
    }

    @Test
    public void testWarnOnStaticInjection() {
        List<Element> elements = Dject.builder().withModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        this.requestStaticInjection(String.class);
                    }
                })
                .withWarnOfStaticInjections()
                .getElements();

        Assert.assertEquals(1, elements.size());
    }

    @Test
    public void testStripStaticInjection() {
        List<Element> elements = Dject.builder().withModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        this.requestStaticInjection(String.class);
                    }
                })
                .withStripStaticInjections()
                .withWarnOfStaticInjections()
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
        Dject.builder().withModule(new ModuleA())
                .withTraceEachModuleSource()
                .build();
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
