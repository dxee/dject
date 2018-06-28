package org.dxee.dject;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.dxee.dject.lifecycle.LifecycleInjectorCreator;
import org.dxee.dject.lifecycle.LifecycleListener;

/**
 * Contract that makes Guice injector creation a pluggable strategy and allows for typed
 * extensions to the Injector within the context of the strategy.  An InjectorCreator
 * may also implement post injector creation operations such as calling {@link LifecycleListener}s
 * prior to returning form createInjector().
 *
 * <p>
 * InjectorCreator can be used directly with a module,
 *<p>
 * <code>
   new LifecycleInjectorCreator().createInjector(new MyApplicationModule());
 * </code>
 * <p>
 * Alternatively, InjectorCreator can be used in conjunction with the {@link InjectorBuilder} DSL
 *<p>
 * <code>
  LifecycleInjector injector = InjectorBuilder
      .fromModule(new MyApplicationModule())
      .overrideWith(new MyApplicationOverrideModule())
      .combineWith(new AdditionalModule()
      .createInjector(new LifecycleInjectorCreator());
  }
 * </code>
 *
 * {@link LifecycleInjectorCreator}
 */
public interface InjectorCreator<I extends Injector> {
    I createInjector(Stage stage, Module module);
}
