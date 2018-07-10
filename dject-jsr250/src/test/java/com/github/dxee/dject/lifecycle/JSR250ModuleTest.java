package com.github.dxee.dject.lifecycle;

import com.github.dxee.dject.Dject;
import com.github.dxee.dject.TestSupport;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JSR250ModuleTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSR250ModuleTest.class);

    static class TestRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TestRuntimeException() {
            super();
        }

        public TestRuntimeException(String message) {
            super(message);
        }

    }

    private enum Events {
        Injected, Initialized, Destroyed, Started, Stopped, Error
    }

    static class TrackingLifecycleListener implements LifecycleListener {
        final List<Events> events = new ArrayList<>();
        private String name;

        public TrackingLifecycleListener(String name) {
            this.name = name;
        }

        @Inject
        public void injected(Injector injector) {
            events.add(Events.Injected);
        }

        @PostConstruct
        public void initialized() {
            events.add(Events.Initialized);
        }

        @Override
        public void onStarted() {
            events.add(Events.Started);
        }

        @Override
        public void onStopped(Throwable t) {
            events.add(Events.Stopped);
            if (t != null) {
                t.printStackTrace();
                events.add(Events.Error);
            }
        }

        @PreDestroy
        public void destroyed() {
            events.add(Events.Destroyed);
        }

        @Override
        public String toString() {
            return "TrackingLifecycleListener@" + name;
        }
    }
    
    @Test
    public void confirmLifecycleListenerEventsForRTExceptionPostConstruct() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Override
            @PostConstruct
            public void initialized() {
                super.initialized();
                throw new TestRuntimeException("postconstruct rt exception");
            }
        };

        try {
            TestSupport.inject(listener);
        } catch (CreationException e) {
            // expected
        } catch (Exception e) {
            fail("expected CreationException starting injector but got " + e);
        } finally {
            assertThat(listener.events,
                equalTo(
                    Arrays.asList(Events.Injected, Events.Initialized, Events.Stopped, Events.Error)
                )
            );
        }
    }

    @Test
    public void confirmLifecycleListenerEventsForRTExceptionPreDestroy() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @PreDestroy
            @Override
            public void destroyed() {
                super.destroyed();
                throw new TestRuntimeException("destroyed rt exception");
            }
        };

        TestSupport.inject(listener).shutdown();
        assertThat(listener.events,
            equalTo(
                Arrays.asList(Events.Injected, Events.Initialized, Events.Started, Events.Stopped, Events.Destroyed)
            )
        );
    }
    
    
    @Test(expected = AssertionError.class)
    public void assertionErrorInPostConstruct() {
        TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @PostConstruct
            @Override
            public void initialized() {
                super.initialized();
                fail("postconstruct exception");
            }
        };
        try {
            TestSupport.inject(listener);
        } finally {
            assertThat(listener.events, equalTo(
                Arrays.asList(Events.Injected, Events.Initialized, Events.Stopped, Events.Error)));
        }
    }
    
    @Test(expected = AssertionError.class)
    public void assertionErrorInPreDestroy() {
        TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @PreDestroy
            @Override
            public void destroyed() {
                super.destroyed();
                fail("expected exception from predestroy");
            }
        };
        try {
            TestSupport.inject(listener).shutdown();
        } finally {
            assertThat(listener.events, equalTo(
                Arrays.asList(Events.Injected, Events.Initialized, Events.Started, Events.Stopped, Events.Destroyed)));
        }

    }
    
    
    public static class Listener1 implements LifecycleListener {
        boolean wasStarted;
        boolean wasStopped;
        
        @Inject
        Provider<Listener2> nestedListener;
        
        @Override
        public void onStarted() {
            LOGGER.info("starting listener1");
            wasStarted = true;
            nestedListener.get();
        }

        @Override
        public void onStopped(Throwable error) {
            LOGGER.info("stopped listener1");
            wasStopped = true;
            Assert.assertTrue(nestedListener.get().wasStopped);

        }
    }
    
    public static class Listener2 implements LifecycleListener {
        boolean wasStarted;
        boolean wasStopped;

        @Override
        public void onStarted() {
            LOGGER.info("starting listener2");
            wasStarted = true;
        }

        @Override
        public void onStopped(Throwable error) {
            LOGGER.info("stopped listener2");
            wasStopped = true;
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
