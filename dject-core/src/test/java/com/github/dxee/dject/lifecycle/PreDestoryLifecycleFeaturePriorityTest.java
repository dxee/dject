package com.github.dxee.dject.lifecycle;

import com.github.dxee.dject.Dject;
import com.github.dxee.dject.lifecycle.impl.AbstractTypeVisitor;
import com.github.dxee.dject.lifecycle.impl.OneAnnotationLifecycleFeature;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
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
        final InOrder inOrder = Mockito.inOrder(testPriority1);

        TestPriority testPriority = null;

        Dject injector = Dject.builder().withModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class)
                        .addBinding().toInstance(
                        new PreDestroyLifecycleFeature1() {
                            @Override
                            public Class<? extends Annotation> annotationClazz() {
                                return PreDestroy1.class;
                            }

                        });
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class)
                        .addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy2.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class)
                        .addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy3.class;
                    }

                    @Override
                    public int priority() {
                        return 3;
                    }
                });
                bind(TestPriority.class).toInstance(testPriority1);
            }
        }).build();
        testPriority = injector.getInstance(TestPriority.class);

        Mockito.verify(testPriority, Mockito.never()).p3();
        Mockito.verify(testPriority, Mockito.never()).p2();
        Mockito.verify(testPriority, Mockito.never()).p1();

        injector.shutdown();

        inOrder.verify(testPriority, Mockito.times(1)).p1();
        inOrder.verify(testPriority, Mockito.times(1)).p2();
        inOrder.verify(testPriority, Mockito.times(1)).p3();
    }

    @Test
    public void confirmPreDestroyOrderWithOverrideParentMethod() {
        final TestPriorityChild testPriorityChild1 = Mockito.spy(new TestPriorityChild());
        final InOrder inOrder = Mockito.inOrder(testPriorityChild1);

        TestPriorityChild testPriorityChild = null;

        Dject injector = Dject.builder().withModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class).addBinding()
                        .toInstance(new PreDestroyLifecycleFeature1() {
                            @Override
                            public Class<? extends Annotation> annotationClazz() {
                                return PreDestroy1.class;
                            }

                            @Override
                            public int priority() {
                                return 3;
                            }
                        });
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class)
                        .addBinding().toInstance(new PreDestroyLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy2.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PreDestroyLifecycleFeature.class)
                        .addBinding().toInstance(new PreDestroyLifecycleFeature1() {
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
        }).build();
        testPriorityChild = injector.getInstance(TestPriorityChild.class);
        Mockito.verify(testPriorityChild, Mockito.never()).p3();

        injector.shutdown();
        // once not twice
        inOrder.verify(testPriorityChild, Mockito.times(1)).p3();
    }

    private abstract static class PreDestroyLifecycleFeature1
            extends OneAnnotationLifecycleFeature
            implements PreDestroyLifecycleFeature {

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
            return new StringBuilder().append("Predestroy @")
                    .append(
                            this.annotationClazz == null ? "null" : this.annotationClazz.getSimpleName()
                    )
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
