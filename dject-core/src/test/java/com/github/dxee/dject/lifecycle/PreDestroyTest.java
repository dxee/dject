package com.github.dxee.dject.lifecycle;

import com.github.dxee.dject.Dject;
import com.github.dxee.dject.TestSupport;
import com.github.dxee.dject.ThreadLocalScope;
import com.github.dxee.dject.ThreadLocalScoped;
import com.github.dxee.dject.feature.DjectFeatures;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;

public class PreDestroyTest {
    private static final int GC_SLEEP_TIME = 100;

    private static class Foo {
        private volatile boolean shutdown = false;

        Foo() {
            System.out.println("Foo constructed: " + this);
        }

        @PreDestroy
        protected void shutdown() {
            shutdown = true;
        }

        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public String toString() {
            return "Foo@" + System.identityHashCode(this);
        }
    }

    @ThreadLocalScoped
    private static class AnnotatedFoo {
        private volatile boolean shutdown = false;

        @SuppressWarnings("unused")
        AnnotatedFoo() {
            System.out.println("AnnotatedFoo constructed: " + this);
        }

        @PreDestroy
        public void shutdown() {
            this.shutdown = true;
        }

        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public String toString() {
            return "AnnotatedFoo@" + System.identityHashCode(this);
        }
    }

    private static class InvalidPreDestroys {
        @PreDestroy
        public String shutdownWithReturnValue() {
            return "invalid return type";
        }

        @PreDestroy
        public static void shutdownStatic() {
            // can't use static method type
            throw new RuntimeException("boom");
        }

        @PreDestroy
        public void shutdownWithParameters(String invalidArg) {
            // can't use method parameters
        }
    }

    private interface PreDestroyInterface {
        @PreDestroy
        public void destroy();
    }

    private static class PreDestroyImpl implements PreDestroyInterface {
        @Override
        public void destroy() {
            // should not be called
        }
    }

    private static class RunnableType implements Runnable {
        @Override
        @PreDestroy
        public void run() {
            // method from interface; will it be called?
        }
    }

    private static class CloseableType implements Closeable {
        @Override
        public void close() throws IOException {
        }

        @PreDestroy
        public void shutdown() {

        }
    }

    private static class PreDestroyParent1 {
        @PreDestroy
        public void shutdown() {

        }
    }

    private static class PreDestroyChild1 extends PreDestroyParent1 {
        @PreDestroy
        public void shutdown() {
            System.out.println("shutdown invoked");
        }
    }

    private static class PreDestroyParent2 {
        @PreDestroy
        public void anotherShutdown() {

        }
    }

    private static class PreDestroyChild2 extends PreDestroyParent2 {
        @PreDestroy
        public void shutdown() {
            System.out.println("shutdown invoked");
        }
    }

    private static class PreDestroyParent3 {
        @PreDestroy
        public void shutdown() {

        }
    }

    private static class PreDestroyChild3 extends PreDestroyParent3 {
        public void shutdown() {
            System.out.println("shutdown invoked");
        }
    }

    private static class MultipleDestroys {
        @PreDestroy
        public void shutdown1() {
            System.out.println("shutdown1 invoked");
        }

        @PreDestroy
        public void shutdown2() {
            System.out.println("shutdown2 invoked");
        }
    }


    private static class EagerBean {
        volatile boolean shutdown = false;
        SingletonBean singletonInstance;

        @Inject
        public EagerBean(SingletonBean singletonInstance) {
            this.singletonInstance = singletonInstance;
        }

        @PreDestroy
        public void shutdown() {
            System.out.println("eager bean shutdown invoked");
            shutdown = true;
            this.singletonInstance.eagerShutdown = true;
        }
    }

    @Singleton
    private static class SingletonBean {
        volatile boolean eagerShutdown = false;
        boolean shutdown = false;

        @PreDestroy
        public void shutdown() {
            System.out.println("singleton bean shutdown invoked");
            shutdown = true;
            Assert.assertTrue(eagerShutdown);
        }
    }


    @Test
    public void testEagerSingletonShutdown() {
        Dject injector = Dject.builder().withModule(new AbstractModule() {
            @Override
            protected void configure() {
                bind(EagerBean.class).asEagerSingleton();
                bind(SingletonBean.class).in(Scopes.SINGLETON);
            }
        }).build();
        final EagerBean eagerBean = injector.getInstance(EagerBean.class);
        final SingletonBean singletonBean = injector.getInstance(SingletonBean.class);
        Assert.assertFalse(eagerBean.shutdown);
        Assert.assertFalse(singletonBean.shutdown);

        injector.shutdown();
        Assert.assertTrue(eagerBean.shutdown);
        Assert.assertTrue(singletonBean.shutdown);
    }


    @Test
    public void testLifecycleShutdownInheritance1() {
        final PreDestroyChild1 preDestroyChild = Mockito.spy(new PreDestroyChild1());
        final InOrder inOrder = Mockito.inOrder(preDestroyChild);

        Dject injector = TestSupport.inject(preDestroyChild);
        Assert.assertNotNull(injector.getInstance(preDestroyChild.getClass()));
        Mockito.verify(preDestroyChild, Mockito.never()).shutdown();

        injector.shutdown();
        // once not twice
        inOrder.verify(preDestroyChild, Mockito.times(1)).shutdown();
    }

    @Test
    public void testLifecycleShutdownInheritance2() {
        final PreDestroyChild2 preDestroyChild = Mockito.spy(new PreDestroyChild2());
        final InOrder inOrder = Mockito.inOrder(preDestroyChild);

        Dject injector = TestSupport.inject(preDestroyChild);
        Assert.assertNotNull(injector.getInstance(preDestroyChild.getClass()));
        Mockito.verify(preDestroyChild, Mockito.never()).shutdown();

        injector.shutdown();
        // once not twice
        inOrder.verify(preDestroyChild, Mockito.times(1)).shutdown();
        inOrder.verify(preDestroyChild, Mockito.times(1)).anotherShutdown();
    }

    @Test
    public void testLifecycleShutdownInheritance3() {
        final PreDestroyChild3 preDestroyChild = Mockito.spy(new PreDestroyChild3());
        final InOrder inOrder = Mockito.inOrder(preDestroyChild);

        Dject injector = TestSupport.inject(preDestroyChild);
        Assert.assertNotNull(injector.getInstance(preDestroyChild.getClass()));
        Mockito.verify(preDestroyChild, Mockito.never()).shutdown();
        injector.shutdown();
        // never, child class overrides method without annotation
        inOrder.verify(preDestroyChild, Mockito.never()).shutdown();
    }

    @Test
    public void testLifecycleMultipleAnnotations() {
        final MultipleDestroys multipleDestroys = Mockito.spy(new MultipleDestroys());

        Dject injector = new TestSupport()
                .withFeature(DjectFeatures.STRICT_JSR250_VALIDATION, true)
                .withSingleton(multipleDestroys)
                .inject();
        Assert.assertNotNull(injector.getInstance(multipleDestroys.getClass()));
        Mockito.verify(multipleDestroys, Mockito.never()).shutdown1();
        Mockito.verify(multipleDestroys, Mockito.never()).shutdown2();

        injector.shutdown();
        // never, multiple annotations should be ignored
        Mockito.verify(multipleDestroys, Mockito.never()).shutdown1();
        Mockito.verify(multipleDestroys, Mockito.never()).shutdown2();
    }

    @Test
    public void testLifecycleDeclaredInterfaceMethod() {
        final RunnableType runnableInstance = Mockito.mock(RunnableType.class);
        final InOrder inOrder = Mockito.inOrder(runnableInstance);

        Dject injector = TestSupport.inject(runnableInstance);
        Assert.assertNotNull(injector.getInstance(RunnableType.class));
        Mockito.verify(runnableInstance, Mockito.never()).run();
        injector.shutdown();
        inOrder.verify(runnableInstance, Mockito.times(1)).run();
    }

    @Test
    public void testLifecycleAnnotatedInterfaceMethod() {
        final PreDestroyImpl impl = Mockito.mock(PreDestroyImpl.class);
        final InOrder inOrder = Mockito.inOrder(impl);

        Dject injector = TestSupport.inject(impl);
        Assert.assertNotNull(injector.getInstance(RunnableType.class));
        Mockito.verify(impl, Mockito.never()).destroy();

        injector.shutdown();
        inOrder.verify(impl, Mockito.never()).destroy();
    }

    @Test
    public void testLifecycleShutdownWithInvalidPreDestroys() {
        final InvalidPreDestroys ipd = Mockito.mock(InvalidPreDestroys.class);

        Dject injector = new TestSupport()
                .withFeature(DjectFeatures.STRICT_JSR250_VALIDATION, true)
                .withSingleton(ipd)
                .inject();
        Assert.assertNotNull(injector.getInstance(InvalidPreDestroys.class));
        Mockito.verify(ipd, Mockito.never()).shutdownWithParameters(Mockito.anyString());
        Mockito.verify(ipd, Mockito.never()).shutdownWithReturnValue();
        injector.shutdown();
        Mockito.verify(ipd, Mockito.never()).shutdownWithParameters(Mockito.anyString());
        Mockito.verify(ipd, Mockito.never()).shutdownWithReturnValue();
    }

    @Test
    public void testLifecycleCloseable() {
        /*final CloseableType closeableType = Mockito.mock(CloseableType.class);
        try {
            Mockito.doThrow(new IOException("boom")).when(closeableType).close();
        } catch (IOException e1) {
            // ignore, mock only
        }

        Dject injector = TestSupport.inject(closeableType);
        Assert.assertNotNull(injector.getInstance(closeableType.getClass()));
        try {
            Mockito.verify(closeableType, Mockito.never()).close();
        } catch (IOException e) {
            // close() called before shutdown and failed
            Assert.fail("close() called before shutdown and  failed");
        }

        injector.shutdown();
        try {
            Mockito.verify(closeableType, Mockito.times(1)).close();
            Mockito.verify(closeableType, Mockito.never()).shutdown();
        } catch (IOException e) {
            // close() called before shutdown and failed
            Assert.fail("close() called after shutdown and  failed");
        }*/
    }

    @Test
    public void testLifecycleShutdown() {
        final Foo foo = Mockito.mock(Foo.class);
        Dject injector = TestSupport.inject(foo);
        Assert.assertNotNull(injector.getInstance(foo.getClass()));
        Mockito.verify(foo, Mockito.never()).shutdown();
        injector.shutdown();
        Mockito.verify(foo, Mockito.times(1)).shutdown();
    }

    @Test
    public void testLifecycleShutdownWithAtProvides() {
        Dject injector = Dject.builder().withModule(new AbstractModule() {
            @Override
            protected void configure() {
            }

            @Provides
            @Singleton
            Foo getFoo() {
                return new Foo();
            }
        }).build();

        final Foo managedFoo = injector.getInstance(Foo.class);
        Assert.assertNotNull(managedFoo);
        Assert.assertFalse(managedFoo.isShutdown());
    }

    @Test
    public void testLifecycleShutdownWithExplicitScope() throws Exception {
        final ThreadLocalScope threadLocalScope = new ThreadLocalScope();

        Dject injector = Dject.builder().withModule(new AbstractModule() {
            @Override
            protected void configure() {
                binder().bind(Foo.class).in(threadLocalScope);
            }
        }).build();

        threadLocalScope.enter();
        final Foo managedFoo = injector.getInstance(Foo.class);
        Assert.assertNotNull(managedFoo);
        Assert.assertFalse(managedFoo.isShutdown());
        threadLocalScope.exit();

        System.gc();
        Thread.sleep(GC_SLEEP_TIME);
        Assert.assertTrue(managedFoo.isShutdown());
    }

    @Test
    public void testLifecycleShutdownWithAnnotatedExplicitScope() throws Exception {
        final ThreadLocalScope threadLocalScope = new ThreadLocalScope();

        Dject injector = Dject.builder().withModules(new AbstractModule() {
                                                         @Override
                                                         protected void configure() {
                                                             binder().bind(Key.get(AnnotatedFoo.class));
                                                         }
                                                     },
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        binder().bindScope(ThreadLocalScoped.class, threadLocalScope);
                    }
                }).build();

        threadLocalScope.enter();
        final AnnotatedFoo managedFoo = injector.getInstance(AnnotatedFoo.class);
        Assert.assertNotNull(managedFoo);
        Assert.assertFalse(managedFoo.shutdown);
        threadLocalScope.exit();

        System.gc();
        Thread.sleep(GC_SLEEP_TIME);
        synchronized (managedFoo) {
            Assert.assertTrue(managedFoo.shutdown);
        }
    }


    @Test
    public void testLifecycleShutdownWithMultipleInScope() throws Exception {
        final ThreadLocalScope scope = new ThreadLocalScope();
        Dject injector = Dject.builder().withModule(new AbstractModule() {
            @Override
            protected void configure() {
                binder().bindScope(ThreadLocalScoped.class, scope);
            }

            @Provides
            @ThreadLocalScoped
            @Named("afoo1")
            protected AnnotatedFoo afoo1() {
                return new AnnotatedFoo();
            }

            @Provides
            @ThreadLocalScoped
            @Named("afoo2")
            protected AnnotatedFoo afoo2() {
                return new AnnotatedFoo();
            }
        }).build();

        scope.enter();
        AnnotatedFoo managedFoo1 = injector.getInstance(Key.get(AnnotatedFoo.class, Names.named("afoo1")));
        Assert.assertNotNull(managedFoo1);
        Assert.assertFalse(managedFoo1.isShutdown());

        AnnotatedFoo managedFoo2 = injector.getInstance(Key.get(AnnotatedFoo.class, Names.named("afoo2")));
        Assert.assertNotNull(managedFoo2);
        Assert.assertFalse(managedFoo2.isShutdown());

        scope.exit();
        System.gc();
        Thread.sleep(GC_SLEEP_TIME);

        Assert.assertTrue(managedFoo1.isShutdown());
        Assert.assertTrue(managedFoo2.isShutdown());
    }


    @Test
    public void testLifecycleShutdownWithSingletonScope() throws Exception {
        Dject injector = Dject.builder().withModule(new AbstractModule() {
            @Override
            protected void configure() {
                binder().bind(Foo.class).in(Scopes.SINGLETON);
            }
        }).build();

        final Foo managedFoo = injector.getInstance(Foo.class);
        Assert.assertNotNull(managedFoo);
        Assert.assertFalse(managedFoo.isShutdown());

        injector.shutdown();
        System.gc();
        Thread.sleep(GC_SLEEP_TIME);
        Assert.assertTrue(managedFoo.isShutdown());
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
