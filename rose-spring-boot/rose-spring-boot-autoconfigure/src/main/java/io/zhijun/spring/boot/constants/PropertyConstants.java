package io.zhijun.spring.boot.constants;

import io.zhijun.core.annotation.Internal;

/**
 * Internal property name constants shared across Rose Spring Boot modules.
 */
@Internal
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
     * Additional Spring Boot auto-configuration classes to exclude.
     */
    String AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME = "rose.autoconfigure.exclude";

    /**
     * Whether classpath artifact collision diagnosis is enabled.
     */
    String ARTIFACTS_COLLISION_ENABLED_PROPERTY_NAME = "rose.diagnostics.artifacts-collision.enabled";

    /**
     * Whether banned artifact checking is enabled.
     */
    String BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME = ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "banned-artifacts.enabled";

    /**
     * Comma-separated list of banned artifact patterns ({@code groupId:artifactId}).
     */
    String BANNED_ARTIFACTS_PROPERTY_NAME = ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "banned-artifacts";

 String ROSE_SPRING_BOOT_WEB_PROPERTY_NAME_PREFIX = ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "web.";
 
 String ROSE_SPRING_BOOT_WEB_ENABLED_PROPERTY_NAME = ROSE_SPRING_BOOT_WEB_PROPERTY_NAME_PREFIX + "enabled";
 
 String ROSE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX = ROSE_SPRING_BOOT_WEB_PROPERTY_NAME_PREFIX + "mvc.";
 
 String ROSE_SPRING_BOOT_WEBMVC_ENABLED_PROPERTY_NAME = ROSE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX + "enabled";
 }
