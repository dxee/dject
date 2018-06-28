package org.dxee.dject.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.dxee.dject.InjectorBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;

public class PreDestoryLifecycleFeaturePriorityTest {
    static class TestPriority {
        public static final String p1p2p3 = "123";
        public static final String p3p2p1 = "321";
        private String p = "";

        @PreDestroy1
        public void p1() {
            p = p + "1";
        }

        @PreDestroy2
        public void p2() {
            p = p + "2";
        }

        @PreDestroy3
        public void p3() {
            p = p + "3";
        }
    }

    @Test
    public void confirmPreDestroyOrder() {
        LifecycleInjector injector = InjectorBuilder.fromModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy1.class;
                    }

                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return  PreDestroy2.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return  PreDestroy3.class;
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
    public void confirmPreDestroyOrder1() {
        LifecycleInjector injector = InjectorBuilder.fromModule(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return  PreDestroy1.class;
                    }

                    @Override
                    public int priority() {
                        return 3;
                    }
                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy2.class;
                    }

                    @Override
                    public int priority() {
                        return 2;
                    }
                });
                Multibinder.newSetBinder(binder(), PostConstructLifecycleFeature.class).addBinding().toInstance(new PostConstructLifecycleFeature() {
                    @Override
                    public Class<? extends Annotation> annotationClazz() {
                        return PreDestroy3.class;
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
}
