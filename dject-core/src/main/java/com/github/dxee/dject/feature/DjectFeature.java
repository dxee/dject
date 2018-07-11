package com.github.dxee.dject.feature;

public class DjectFeature<T> {
    private final String key;
    private final T defaultValue;
    
    public static <T> DjectFeature<T> create(String key, T defaultValue) {
        return new DjectFeature<T>(key, defaultValue);
    }
    
    public DjectFeature(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }
    
    public String getKey() {
        return key;
    }
    
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) defaultValue.getClass();
    }
    
    public T getDefaultValue() {
        return defaultValue;
    }
}
