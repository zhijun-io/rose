package io.zhijun.boot.autoconfigure;

/**
 * Property names for Rose auto-configuration exclusion.
 */
public final class RoseAutoConfigurationExcludeProperties {

    /**
     * Comma-separated auto-configuration class names to exclude.
     * <p>
     * Unlike {@code spring.autoconfigure.exclude}, values from multiple {@code rose/default/*}
     * resources and property sources are accumulated rather than overwritten.
     */
    public static final String EXCLUDE = "rose.autoconfigure.exclude";

    private RoseAutoConfigurationExcludeProperties() {
    }
}
