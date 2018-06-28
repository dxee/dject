package org.dxee.dject.lifecycle;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.dxee.dject.InjectorBuilder;
import org.dxee.dject.TestSupport;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.Scopes;

public class LifecycleModuleTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleModuleTest.class);

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
        Injected, Started, Stopped, Error
    }

    @Rule
    public final TestName name = new TestName();

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
        }

        @Override
        public String toString() {
            return "TrackingLifecycleListener@" + name;
        }
    }

    @Test
    public void confirmLifecycleListenerEventsForSuccessfulStart() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName());

        TestSupport.inject(listener).close();

        assertThat(listener.events, equalTo(
                Arrays.asList(Events.Injected, Events.Started, Events.Stopped)));
    }

    @Test
    public void confirmLifecycleListenerEventsForRTExceptionInjected() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Override
            @Inject
            public void injected(Injector injector) {
                super.injected(injector);
                throw new TestRuntimeException("injected failed");
            }
        };

        try (LifecycleInjector injector = TestSupport.inject(listener)) {
            fail("expected exception injecting instance");
        } catch (CreationException e) {
            // expected
        } catch (Exception e) {
            fail("expected CreationException injecting instance but got " + e);
        } finally {
            assertThat(listener.events, equalTo(Arrays.asList(Events.Injected)));
        }
    }

    @Test
    public void confirmLifecycleListenerEventsForRTExceptionOnStarted() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Override
            public void onStarted() {
                super.onStarted();
                throw new TestRuntimeException("onStarted rt exception");
            }
        };

        try (LifecycleInjector injector = TestSupport.inject(listener)) {
            fail("expected rt exception starting injector");
        } catch (TestRuntimeException e) {
            // expected
        } catch (Exception e) {
            fail("expected TestRuntimeException starting injector but got " + e);
        } finally {
            assertThat(listener.events, equalTo(Arrays.asList(Events.Injected, Events.Started, Events.Stopped, Events.Error)));
        }
    }

    @Test
    public void confirmLifecycleListenerEventsForRTExceptionOnStopped() {
        final TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Override
            public void onStopped(Throwable t) {
                super.onStopped(t);
                throw new TestRuntimeException("onStopped rt exception");
            }
        };

        try (LifecycleInjector injector = TestSupport.inject(listener)) {
        } finally {
            assertThat(listener.events, equalTo(Arrays.asList(Events.Injected, Events.Started, Events.Stopped)));
        }
    }
    

    @Test(expected=AssertionError.class)
    public void assertionErrorInInject() {
        TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Inject
            @Override
            public void injected(Injector injector) {
                super.injected(injector);
                fail("injected exception");
            }
        };
        try (LifecycleInjector injector = TestSupport.inject(listener)) {
            fail("expected error provisioning injector");
        } catch (Exception e) {
            fail("expected AssertionError provisioning injector but got " + e);
        }
        finally {
            assertThat(listener.events, equalTo(
                Arrays.asList(Events.Injected, Events.Stopped, Events.Error)));
        }
    }
    
    @Test(expected=AssertionError.class)
    public void assertionErrorInOnStarted() {
        TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Override
            public void onStarted() {
                super.onStarted();
                fail("onStarted exception");
            }
        };
        try (LifecycleInjector injector = TestSupport.inject(listener)) {
            fail("expected AssertionError starting injector");
        } catch (Exception e) {
            fail("expected AssertionError starting injector but got " + e);
        }
        finally {
            assertThat(listener.events, equalTo(
                Arrays.asList(Events.Injected, Events.Started, Events.Stopped, Events.Error)));
        }
    }    

    
    
    @Test(expected=AssertionError.class)
    public void assertionErrorInOnStopped() {
        TrackingLifecycleListener listener = new TrackingLifecycleListener(name.getMethodName()) {
            @Override
            public void onStopped(Throwable t) {
                super.onStopped(t);
                fail("onstopped exception");
            }
        };
        try (LifecycleInjector injector = TestSupport.inject(listener)) {
            fail("expected AssertionError stopping injector");
        } catch (Exception e) {
            fail("expected AssertionError stopping injector but got " + e);
        }
        finally {
            assertThat(listener.events, equalTo(
                Arrays.asList(Events.Injected, Events.Stopped, Events.Error)));
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

    @Test
    public void testNestedLifecycleListeners() {
        Listener1 listener1;
        Listener2 listener2;
        try (LifecycleInjector injector = InjectorBuilder.fromModule(new AbstractModule() {
            
            @Override
            protected void configure() {
                bind(Listener1.class).asEagerSingleton();
                bind(Listener2.class).in(Scopes.SINGLETON);                
            }
        }).createInjector()) {
            listener1 = injector.getInstance(Listener1.class);
            listener2 = listener1.nestedListener.get();
            Assert.assertNotNull(listener2);
            Assert.assertTrue(listener1.wasStarted);
            Assert.assertTrue(listener2.wasStarted);
        }
        Assert.assertTrue(listener1.wasStopped);
        Assert.assertTrue(listener2.wasStopped);
    }

}
