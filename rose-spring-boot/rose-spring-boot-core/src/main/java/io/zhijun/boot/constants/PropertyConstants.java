package io.zhijun.boot.constants;

/**
 * Property name constants for Rose Spring Boot.
 */
public interface PropertyConstants {

    /**
     * Property prefix {@code rose.spring.boot.}.
     */
    String ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX = "rose.spring.boot.";

    /**
     * Whether classpath {@code config/default/*} merging is enabled.
     */
    String DEFAULT_CONFIG_ENABLED_PROPERTY_NAME = "rose.default-config.enabled";

    /**
     * Additional classpath location patterns for default config merging.
     */
    String DEFAULT_CONFIG_LOCATIONS_PROPERTY_NAME = "rose.default-config.locations";

    /**
     * Whether classpath artifact collision diagnosis is enabled.
     */
    String ARTIFACTS_COLLISION_ENABLED_PROPERTY_NAME = "rose.diagnostics.artifacts-collision.enabled";
}
