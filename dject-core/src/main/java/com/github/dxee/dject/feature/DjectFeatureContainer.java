package com.github.dxee.dject.feature;

/**
 * Container of Dject features.
 */
public interface DjectFeatureContainer {
    /**
     * @return Get the value of the feature or the default if none is set
     */
    <T> T get(DjectFeature<T> feature);
}
