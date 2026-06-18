package io.zhijun.boot.env.defaults;

/**
 * Configuration keys and conventions for Rose module default configuration.
 * <p>
 * Modules place files under {@code src/main/resources/rose/default/} on the classpath
 * ({@code .properties}, {@code .yml}, {@code .yaml}). YAML lists become indexed keys;
 * leaf values are normalized to strings. See {@link DefaultConfigPropertiesLoader}.
 * ({@code .properties}, {@code .yml}, or {@code .yaml}). Values are merged into Spring Boot
 * {@code defaultProperties} (lowest precedence).
 */
public final class DefaultConfigProperties {

    public static final String CONFIG_PREFIX = "rose.default-config";

    /**
     * Whether Rose module default config loading is enabled.
     * <p>
     * Evaluated in {@link DefaultConfigPropertiesEnvironmentPostProcessor} before
     * {@code application.yml} is loaded. Use a system property or environment variable
     * (for example {@code -Drose.default-config.enabled=false} or
     * {@code ROSE_DEFAULT_CONFIG_ENABLED=false}), not {@code application.yml} alone.
     */
    public static final String ENABLED = CONFIG_PREFIX + ".enabled";

    public static final String LOCATIONS = CONFIG_PREFIX + ".locations";

    public static final String DEFAULT_PROPERTIES_PATTERN = "classpath*:rose/default/*.properties";

    public static final String DEFAULT_YML_PATTERN = "classpath*:rose/default/*.yml";

    public static final String DEFAULT_YAML_PATTERN = "classpath*:rose/default/*.yaml";

    public static final String[] DEFAULT_LOCATION_PATTERNS = {
            DEFAULT_PROPERTIES_PATTERN,
            DEFAULT_YML_PATTERN,
            DEFAULT_YAML_PATTERN
    };

    private DefaultConfigProperties() {
    }
}
