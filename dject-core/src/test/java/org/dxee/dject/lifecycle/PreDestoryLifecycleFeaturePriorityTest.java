package org.dxee.dject.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import org.dxee.dject.Dject;
import org.dxee.dject.DjectBuilder;
import org.dxee.dject.lifecycle.impl.AbstractTypeVisitor;
import org.dxee.dject.lifecycle.impl.OneAnnotationLifecycleFeature;
import org.dxee.dject.trace.TracingProvisionListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;

public class PreDestoryLifecycleFeaturePriorityTest {
    private static class TestPriority {
        @PreDestroy1
        public void p1() {
        }

        @PreDestroy2
        public void p2() {
        }

        @PreDestroy3
        public void p3() {
            System.out.println("TestPriority p3");
        }
    }

    private static class TestPriorityChild extends TestPriority {
        @PreDestroy3
        public void p3() {
            System.out.println("TestPriorityChild p3");
        }
    }

    @Test
    public void confirmPreDestroyOrder() {
        final TestPriority testPriority1 = Mockito.spy(new TestPriority());
        InOrder inOrder = Mockito.inOrder(testPriority1);

        TestPriority testPriority = null;
        try(Dject injector = DjectBuilder.fromModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy1.class;
                    }

                });
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return  PreDestroy2.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return  PreDestroy3.class;
                    }

                    @Override
                    public int priority() {
                        return 3;
                    }
                });
                bind(TestPriority.class).toInstance(testPriority1);
                bindListener(Matchers.any(), TracingProvisionListener.createDefault());
            }
        }).createInjector()) {
            testPriority = injector.getInstance(TestPriority.class);

            Mockito.verify(testPriority, Mockito.never()).p3();
            Mockito.verify(testPriority, Mockito.never()).p2();
            Mockito.verify(testPriority, Mockito.never()).p1();
        };

        inOrder.verify(testPriority, Mockito.times(1)).p1();
        inOrder.verify(testPriority, Mockito.times(1)).p2();
        inOrder.verify(testPriority, Mockito.times(1)).p3();
    }

    @Test
    public void confirmPreDestroyOrderWithOverrideParentMethod() {
        final TestPriorityChild testPriorityChild1 = Mockito.spy(new TestPriorityChild());
        InOrder inOrder = Mockito.inOrder(testPriorityChild1);

        TestPriorityChild testPriorityChild = null;
        try (Dject injector = DjectBuilder.fromModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return  PreDestroy1.class;
                    }

                    @Override
                    public int priority() {
                        return 3;
                    }
                });
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy2.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy3.class;
                    }

                    @Override
                    public int priority() {
                        return 1;
                    }
                });
                bind(TestPriorityChild.class).toInstance(testPriorityChild1);
            }
        }).createInjector()) {
            testPriorityChild = injector.getInstance(TestPriorityChild.class);
            Mockito.verify(testPriorityChild, Mockito.never()).p3();
        }

        // once not twice
        inOrder.verify(testPriorityChild, Mockito.times(1)).p3();
    }

    private static abstract class PreDestroyLifecycleFeature1 extends OneAnnotationLifecycleFeature implements PreDestroyLifecycleFeature {

        @Override
        public PreDestroyTypeVisitor visitor() {
            return new PreDestroyTypeVisitor(this.annotationClazz);
        }

        private class PreDestroyTypeVisitor extends AbstractTypeVisitor {
            public PreDestroyTypeVisitor(Class<? extends Annotation> annotationClazz) {
                super(annotationClazz);
            }

            @Override
            public void addMethodLifecycleAction(LifecycleAction lifecycleAction) {
                addLifecycleActionToLastOne(lifecycleAction);
            }
        }

        @Override
        public String toString() {
            return new StringBuilder().append("Predestroy @").append(this.annotationClazz==null ? "null" : this.annotationClazz.getSimpleName())
                    .append(" with priority ").append(priority()).toString();
        }
    }

    @Before
    public void printTestHeader() {
        System.out.println("\n=======================================================");
        System.out.println("  Running Test : " + name.getMethodName());
        System.out.println("=======================================================\n");
    }

    @Rule
    public final TestName name = new TestName();
}
