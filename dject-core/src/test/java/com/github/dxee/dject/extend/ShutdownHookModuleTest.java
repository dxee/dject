package com.github.dxee.dject.extend;

import com.github.dxee.dject.Dject;
import com.github.dxee.dject.lifecycle.LifecycleListener;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for ShutdownHookModule, there is no asserts, just system out.
 * @author bing.fan
 * 2018-07-02 14:06
 */
public class ShutdownHookModuleTest {
    private static Logger LOGGER = LoggerFactory.getLogger(ShutdownHookModuleTest.class);
    @Rule
    public final TestName name = new TestName();

    private enum Events {
        Injected, Started, Stopped, Error
    }

    public static class TrackingLifecycleListener implements LifecycleListener {
        final List<Events> events = new ArrayList<>();
        protected String name;

        public TrackingLifecycleListener(String name) {
            this.name = name;
        }

        @Inject
        public void injected(Injector injector) {
            events.add(Events.Injected);
        }


        @Override
        public void onStarted() {
            events.add(Events.Started);
        }

        @Override
        public void onStopped(Throwable t) {
            events.add(Events.Stopped);
            if (t != null) {
                events.add(Events.Error);
            }

            events.forEach((evt) -> {
                LOGGER.info(name + "------------>" + evt.name());
            });
        }

        @Override
        public String toString() {
            return "TrackingLifecycleListener@" + name;
        }
    }

    static class TestRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TestRuntimeException() {
            super();
        }

        public TestRuntimeException(String message) {
            super(message);
        }

    }

    @Test
    public void confirmLifecycleListenerShutdown() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName());

        Dject.builder().withModules(new ShutdownHookModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(TrackingLifecycleListener.class).toInstance(listener);
                    }
                }).build();
    }

    @Test
    public void confirmMultiLifecycleListenerShutdown() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName());
        final TrackingLifecycleListener listener1 = new TrackingLifecycleListener(name.getMethodName() + "1");

        Dject.builder().withModules(new ShutdownHookModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                .addBinding().toInstance(listener);
                        Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                .addBinding().toInstance(listener1);
                    }
                }).build();
    }

    @Test
    public void confirmMultiLifecycleListenerShutdownWithOneStopRTException() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName());
        final TrackingLifecycleListener listener1 = new TrackingLifecycleListener(name.getMethodName() + "1") {
          @Override
          public void onStopped(Throwable t) {
              events.forEach((evt) -> {
                  LOGGER.info(name + "------------>" + evt.name());
              });

              throw new TestRuntimeException("RT at onStopped.");
          }
        };

        Dject.builder().withModules(new ShutdownHookModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                .addBinding().toInstance(listener);
                        Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                .addBinding().toInstance(listener1);
                    }
                }).build().shutdown();

        assertThat(listener.events,
                equalTo(Arrays.asList(Events.Injected, Events.Started, Events.Stopped))
        );

        assertThat(listener1.events,
                equalTo(Arrays.asList(Events.Injected, Events.Started))
        );
    }

    // ShutdownHookModule will run onStopped for listener1, but not run onStopped for listener
    // due to listener1 Error
    @Test
    public void confirmMultiLifecycleListenerShutdownWithOneStopError() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName());
        final TrackingLifecycleListener listener1 = new TrackingLifecycleListener(name.getMethodName() + "1") {
            @Override
            public void onStopped(Throwable t) {
                events.forEach((evt) -> {
                    LOGGER.info(name + "------------>" + evt.name());
                });
                fail("ERROR at onStopped.");
            }
        };

        try {
            Dject.builder().withModules(new ShutdownHookModule(),
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                    .addBinding().toInstance(listener);
                            Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                    .addBinding().toInstance(listener1);
                        }
                    }).build();
        } catch (AssertionError e) {
            // expected
        }

        assertThat(listener.events,
                equalTo(Arrays.asList(Events.Injected, Events.Started))
        );

        assertThat(listener1.events,
                equalTo(Arrays.asList(Events.Injected, Events.Started))
        );
    }

    /**
     * ShutdownHookModule will run onStopped for listener and listener1.
     * ShutdownHookModule will no longer run LifecycleManager due to it has been stoped.
    */
    @Test
    public void confirmMultiLifecycleListenerStartedWithOneStartRTException() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Override
            public void onStarted() {
                throw new TestRuntimeException("RT at onStarted.");
            }
        };
        final TrackingLifecycleListener listener1 = new TrackingLifecycleListener(name.getMethodName() + "1");

        try {
            Dject.builder().withModules(new ShutdownHookModule(),
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                    .addBinding().toInstance(listener);
                            Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                    .addBinding().toInstance(listener1);
                        }
                    }).build();
        } catch (TestRuntimeException e) {
            // expected
        }

        assertThat(listener.events,
                equalTo(Arrays.asList(Events.Injected, Events.Stopped, Events.Error))
        );

        assertThat(listener.events,
                equalTo(Arrays.asList(Events.Injected, Events.Stopped, Events.Error))
        );
    }

    /**
     * If listener is called before listener1 in the LifecycleManager,  listener throws Error in
     * onStarted, then LifecycleManager will stop to call listener1.onStarted() and alse not call to onStop，
     * but will call onStopped through ShutdownHookModule.
     */
    @Test
    public void confirmMultiLifecycleListenerStartedWithOneStartError() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Override
            public void onStarted() {
                fail("ERROR at onStarted.");
            }
        };
        final TrackingLifecycleListener listener1 = new TrackingLifecycleListener(name.getMethodName() + "1");
        try {
            Dject.builder().withModules(new ShutdownHookModule(),
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                    .addBinding().toInstance(listener);
                            Multibinder.newSetBinder(binder(), TrackingLifecycleListener.class)
                                    .addBinding().toInstance(listener1);
                        }
                    }).build();
        } catch (AssertionError e) {
            // expected
        }

        assertThat(listener.events,
                equalTo(Arrays.asList(Events.Injected))
        );

        assertThat(listener1.events,
                equalTo(Arrays.asList(Events.Injected))
        );
    }
}