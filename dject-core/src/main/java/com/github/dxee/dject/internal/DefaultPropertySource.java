package com.github.dxee.dject.internal;

import com.github.dxee.dject.annotations.SuppressLifecycleUninitialized;
import com.github.dxee.dject.extend.AbstractPropertySource;

import javax.inject.Singleton;

/**
 * PropertySource based on system and environment properties with 
 * system properties having precedence. 
 * 
 * @author elandau
 *
 */
@Singleton
@SuppressLifecycleUninitialized
public final class DefaultPropertySource extends AbstractPropertySource {

    @Override
    public String get(String key) {
        return get(key, (String)null);
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
            if (value == null) {
                return defaultValue;
            }
        }
        return value;
    }

    @Override
    public boolean hasProperty(String key) {
        return get(key, (String)null) != null;
    }
}
