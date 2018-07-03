package org.dxee.dject.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.dxee.dject.DjectBuilder;
import org.dxee.dject.Dject;
import org.dxee.dject.lifecycle.impl.AbstractTypeVisitor;
import org.dxee.dject.lifecycle.impl.OneAnnotationLifecycleFeature;
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
        private String p = "";

        @PostConstruct1
        public void p1() {
            p = p + "1";
        }

        @PostConstruct3
        public void p2() {
            p = p + "2";
        }

        @PostConstruct2
        public void p3() {
            p = p + "3";
        }
    }

    @Test
    public void confirmPostConstructOrder() {
        Dject injector = DjectBuilder.fromModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PostConstruct1.class;
                    }

                    @Override
                    public int priority() {
                        return 1;
                    }
                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PostConstruct3.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature1() {
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
        }).createInjector();

        TestPriority testPriority = injector.getInstance(TestPriority.class);

        Assert.assertTrue(testPriority.p.equals(TestPriority.p1p2p3));
    }

    @Test
    public void confirmPostConstructOrder1() {
        Dject injector = DjectBuilder.fromModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PostConstruct1.class;
                    }

                    @Override
                    public int priority() {
                        return 3;
                    }
                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature1() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PostConstruct3.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature1() {
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
        }).createInjector();

        TestPriority testPriority = injector.getInstance(TestPriority.class);

        Assert.assertTrue(testPriority.p.equals(TestPriority.p3p2p1));
    }


    private static abstract class PostConstructLifecycleFeature1 extends OneAnnotationLifecycleFeature implements PostConstructLifecycleFeature {
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
