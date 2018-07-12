package com.github.dxee.dject.feature;

/**
 * Core Dject features.  Features are configured/enabled on {@link com.github.dxee.dject.Dject}
 */
public final class DjectFeatures {
    /**
     * When disable the Dject process will continue running even if there is a catastrophic
     * startup failure.  This allows the admin page to stay up so that the process may be 
     * debugged more easily. 
     */
    public static final DjectFeature<Boolean> SHUTDOWN_ON_ERROR =
            DjectFeature.create("dject.features.shutdownOnError", true);

    /**
     * Enables @PostConstruct / @PreDestroy support
     */
    public static final DjectFeature<Boolean> JSR250_SUPPORT =
            DjectFeature.create("dject.features.jsr250support", true);

    /**
     * Enables strict validation of @PostConstruct / @PreDestroy annotations at runtime; default is false
     */
    public static final DjectFeature<Boolean> STRICT_JSR250_VALIDATION =
            DjectFeature.create("dject.features.strictJsr250Validation", false);

    /**
     * Enables predestroy autocloseable
     */
    public static final DjectFeature<Boolean> PREDESTROY_AUTOCLOSEABLE =
            DjectFeature.create("dject.features.predestroyautocloseable", true);
    
}
