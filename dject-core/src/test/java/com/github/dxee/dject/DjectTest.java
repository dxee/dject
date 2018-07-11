package com.github.dxee.dject;

import com.github.dxee.dject.extend.ShutdownHookModule;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;

import javax.annotation.PreDestroy;
import java.util.List;

import static com.google.inject.name.Names.named;

public class DjectTest {
    @Test
    public void testBindingTracing() {
        Dject.builder().withModule(
                new AbstractModule() {
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
        Dject li = Dject.builder().withModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(String.class).toInstance("Hello world");
                    }
                })
                .withWarnOfToInstanceInjections()
                .withTraceEachKey()
                .build();
    }

    @Test
    public void testWarnOnStaticInjection() {
        List<Element> elements = Dject.builder().withModule(
                new AbstractModule() {
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
        List<Element> elements = Dject.builder().withModule(
                new AbstractModule() {
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

    @Test
    public void testTracingProvision() {
        Dject.builder().withModule(new ModuleA())
                .withTracingProvision()
                .build();
    }

    @Test
    public void testSingletonScopInstanceAutoCloseable() {
        Dject inject = Dject.builder().withModules(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(A.class).in(Scopes.SINGLETON);
                        bind(C.class).in(Scopes.SINGLETON);
                    }
                })
                .build();

        A a = inject.getInstance(A.class);
        C c = inject.getInstance(C.class);

        inject.getInstance(A.class);
        inject.getInstance(C.class);

        inject.shutdown();

        Assert.assertEquals(1, a.count);
        Assert.assertEquals(1, c.count);

    }

    @Test
    public void testNoScopInstanceAutoCloseable() {
        Dject inject = Dject.builder().withModules(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(A.class);
                        bind(C.class);
                    }
                })
                .build();

        A a = inject.getInstance(A.class);
        C c = inject.getInstance(C.class);

        inject.getInstance(A.class);
        inject.getInstance(C.class);

        inject.shutdown();

        Assert.assertEquals(1, c.count);
        Assert.assertEquals(1, a.count);
    }

    @Test
    public void testClassLoaderShutdownInTheRightWay() {
        Dject inject = Dject.builder().withModules(new ShutdownHookModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ClassLoader.class).toInstance(getDefaultClassLoader());
                        bind(A.class).in(Scopes.SINGLETON);
                        bind(C.class).in(Scopes.SINGLETON);
                    }
                })
                .build();
        inject.getInstance(A.class);
        inject.getInstance(C.class);
    }

    private ClassLoader getDefaultClassLoader() {
        return getClass().getClassLoader();
    }

    private static class A implements AutoCloseable {
        public int count = 0;
        @Override
        @PreDestroy
        public void close() {
            count++;
            throw new RuntimeException("A tst");
        }
    }

    private static class C implements AutoCloseable {
        public int count = 0;
        @Override
        @PreDestroy
        public void close() {
            count++;
            throw new RuntimeException("C tst");
        }
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
