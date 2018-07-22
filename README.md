# Dject
Guice extensions, help to improve the developer experience of guice. 

## Design Principles
Keep the core small and scalable, and provide rich extensions arround the core.

## Lifecycle support  
You can use `@PostConstruct` and `@PreDestroy` on the method.  
```java
public class PostConstructTest {
    @PostConstruct
    public void postConstruct() {
        // More code here 
    }
    
    @PreDestroy
    public void PreDestroy() {
        // More code here 
    }
}
```  
You can also implements `LifecycleListener`.  
```java
public class ListenerTest implements LifecycleListener {

    @Override
    public void onStarted() {
        // More code here
    }

    @Override
    public void onStopped(Throwable error) {
        // More code here
    }
}
```   
- `@PostConstruct` excute at guice object provision time, `LifecycleListener.onStarted` excute when create guice 
injecter finished. `@PostConstruct` will fail fast at guice provision time.   
- `LifecycleListener.onStopped` excuted before `@PreDestroy` when object destroyed.  

> **Be Carefull**  
Any `Error`(but not `Exception`) from the lifecycle method will stop executing more lifecycle methods directly.
Which means `LifecycleListener.onStopped` and `@PreDestroy` will not be excuted if any destroy lifecycle method 
before throws `Error`. 

## Usage  
User builder pattern to start up `Dject`.  
```java
public class DjectTest {
    @Test
    public void testStartUp() {
        Dject.newBuilder().withModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(String.class).toInstance("Hello world");
                    }
                })
                .build();
    }
}
```  
If you do not want to abort main thread， call `awaitShutdown()` after Dject build.  
```java
public class DjectTest {
    @Test
    public void testAwaitShutdown() {
        Dject.newBuilder().withModule(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(String.class).toInstance("Hello world");
                    }
                })
                .build().awaitShutdown();
    }
}
```  
## Contribution
PR is very welcome.