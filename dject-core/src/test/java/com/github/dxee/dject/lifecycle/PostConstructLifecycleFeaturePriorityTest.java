package com.github.dxee.dject.lifecycle;

import com.github.dxee.dject.Dject;
import com.github.dxee.dject.lifecycle.impl.AbstractTypeVisitor;
import com.github.dxee.dject.lifecycle.impl.OneAnnotationLifecycleFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.lang.annotation.Annotation;

public class PostConstructLifecycleFeaturePriorityTest {
    static class TestPriority {
        public static final String p1p2p3 = "123";
        public static final String p3p2p1 = "321";
        private String priority = "";

        @PostConstruct1
        public void p1() {
            priority = priority + "1";
        }

        @PostConstruct3
        public void p2() {
            priority = priority + "2";
        }

        @PostConstruct2
        public void p3() {
            priority = priority + "3";
        }
    }

    @Test
    public void confirmPostConstructOrder() {
        Dject injector = Dject.builder().withModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class)
                        .addBinding().toInstance(
                        new PostConstructLifecycleFeature1() {
                            @Override
                            public Class<? extends Annotation> annotationClazz() {
                                return PostConstruct1.class;
                            }

                            @Override
                            public int priority() {
                                return 1;
                            }
                        });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class)
                        .addBinding().toInstance(new PostConstructLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PostConstruct3.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class)
                        .addBinding().toInstance(new PostConstructLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PostConstruct2.class;
                    }

                    @Override
                    public int priority() {
                        return 3;
                    }
                });
                bind(TestPriority.class).in(Scopes.SINGLETON);
            }
        }).build();

        TestPriority testPriority = injector.getInstance(TestPriority.class);

        Assert.assertTrue(testPriority.priority.equals(TestPriority.p1p2p3));
    }

    @Test
    public void confirmPostConstructOrder1() {
        Dject injector = Dject.builder().withModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class)
                                .addBinding().toInstance(new PostConstructLifecycleFeature1() {
                            @Override
                            public Class<? extends Annotation> annotationClazz() {
                                return PostConstruct1.class;
                            }

                            @Override
                            public int priority() {
                                return 3;
                            }
                        });
                        Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class)
                                .addBinding().toInstance(new PostConstructLifecycleFeature1() {
                            @Override
                            public Class<? extends Annotation> annotationClazz() {
                                return PostConstruct3.class;
                            }

                            @Override
                            public int priority() {
                                return 2;
                            }
                        });
                        Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class)
                                .addBinding().toInstance(new PostConstructLifecycleFeature1() {
                            @Override
                            public Class<? extends Annotation> annotationClazz() {
                                return PostConstruct2.class;
                            }

                            @Override
                            public int priority() {
                                return 1;
                            }
                        });
                        bind(TestPriority.class).in(Scopes.SINGLETON);
                    }
                }).build();

        TestPriority testPriority = injector.getInstance(TestPriority.class);

        Assert.assertTrue(testPriority.priority.equals(TestPriority.p3p2p1));
    }


    private abstract static class PostConstructLifecycleFeature1
            extends OneAnnotationLifecycleFeature
            implements PostConstructLifecycleFeature {
        @Override
        public TestPostConstructTypeVisitor visitor() {
            return new TestPostConstructTypeVisitor(this.annotationClazz);
        }

        private class TestPostConstructTypeVisitor extends AbstractTypeVisitor {
            public TestPostConstructTypeVisitor(Class<? extends Annotation> annotationClazz) {
                super(annotationClazz);
            }

            @Override
            public void addMethodLifecycleAction(LifecycleAction lifecycleAction) {
                addLifecycleActionToFirstOne(lifecycleAction);
            }
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
