package org.dxee.dject;

import com.google.inject.AbstractModule;
import com.google.inject.spi.Element;
import org.dxee.dject.visitors.BindingTracingVisitor;
import org.dxee.dject.visitors.KeyTracingVisitor;
import org.dxee.dject.visitors.ModuleSourceTracingVisitor;
import org.dxee.dject.visitors.WarnOfToInstanceInjectionVisitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

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
            .forEachElement(new BindingTracingVisitor(), message -> LoggerFactory.getLogger(this.getClass()).debug(message))
            .createInjector();
    }
    
    @Test
    public void testForEachBinding() {
        Consumer<String> consumer = Mockito.mock(Consumer.class);
        DjectBuilder
            .fromModule(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(String.class).toInstance("Hello world");
                }
            })
            .forEachElement(new WarnOfToInstanceInjectionVisitor(), consumer)
            .createInjector();
        
        Mockito.verify(consumer, Mockito.times(1)).accept(Mockito.anyString());
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
            .forEachElement(new KeyTracingVisitor(), message -> LoggerFactory.getLogger(this.getClass()).debug(message))
            .createInjector()) {}
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
            .forEachElement(new ModuleSourceTracingVisitor(), message -> LoggerFactory.getLogger(this.getClass()).debug(message))
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
