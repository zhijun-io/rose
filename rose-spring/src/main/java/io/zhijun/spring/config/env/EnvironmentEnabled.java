
package io.zhijun.spring.config.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import static io.zhijun.spring.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.zhijun.spring.constants.PropertyConstants.ROSE_SPRING_PROPERTY_NAME_PREFIX;

/**
 * Abstract base class for components that are enabled or disabled based on {@link Environment}.
 *
 * <p>Extensions define enable/disable behavior through configuration properties:
 * the property name is derived from the subclass name via {@link #getEnabledPropertyName()},
 * and {@link #isEnabled(Environment)} checks that property with a fallback to
 * {@link #getDefaultEnabled()} (default: {@code true}).
 *
 * <h3>Design Rationale</h3>
 * <p>This was originally an interface with {@code default} methods. The problem is that
 * a Logger inside a {@code default} method calls {@link LoggerFactory#getLogger(Class)}
 * on <em>every invocation</em>, creating unnecessary object churn. By converting to an
 * abstract class, the Logger becomes a one-time instance field, initialized once per
 * subclass instance. The tradeoff is that subclasses cannot extend another class, but in
 * practice this is a template base — extensions that need multiple inheritance can extract
 * the property-name logic into a utility.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * public class MyMBeanExporter extends EnvironmentEnabled {
 *     public String getEnabledPropertyName() {
 *         return "myapp.mbean-exporter.enabled";
 *     }
 * }
 *
 * // Usage:
 * EnvironmentEnabled component = new MyMBeanExporter();
 * if (component.isEnabled(environment)) {
 *     // proceed with initialization
 * }
 * }</pre>
 *
 * @see Environment
 * @since 1.0.0
 */
public abstract class EnvironmentEnabled {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected Logger getLogger() {
        return logger;
    }

    /**
     * Checks if this component is enabled based on the given {@link Environment}.
     *
     * @param environment the Spring {@link Environment} to check against, must not be {@code null}
     * @return {@code true} if the component is enabled, {@code false} otherwise
     * @see #getEnabledPropertyName()
     * @see #getDefaultEnabled()
     */
    public boolean isEnabled(Environment environment) {
        String enabledPropertyName = getEnabledPropertyName();
        boolean enabled = environment.getProperty(enabledPropertyName, boolean.class, getDefaultEnabled());
        Logger log = getLogger();
        if (enabled) {
            if (log.isTraceEnabled()) {
                log.trace("The {} is enabled, if it needs to be disabled[default : '{}'], please set the property '{}' to 'false' .",
                        getClass().getSimpleName(), getDefaultEnabled(), getEnabledPropertyName());
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("The {} is disabled, if it needs to be enabled[default : '{}'], please set the property '{}' to 'true' .",
                        getClass().getSimpleName(), getDefaultEnabled(), getEnabledPropertyName());
            }
        }
        return enabled;
    }

    /**
     * Gets the property name used to determine if this component is enabled.
     *
     * <p>The default implementation derives the name from the subclass's simple name:
     * {@code rose.spring.<SimpleClassName>.enabled}.
     * Subclasses may override to provide a custom property path.
     *
     * @return the property name key for checking the enabled status
     */
    public String getEnabledPropertyName() {
        String className = this.getClass().getSimpleName();
        return ROSE_SPRING_PROPERTY_NAME_PREFIX + className + '.' + ENABLED_PROPERTY_NAME;
    }

    /**
     * Gets the default enabled status for this component.
     *
     * <p>Returned when the property from {@link #getEnabledPropertyName()} is not set
     * in the {@link Environment}. Defaults to {@code true}; subclasses may override
     * to disable the component by default.
     *
     * @return {@code true} if the component is enabled by default, {@code false} otherwise
     * @see #isEnabled(Environment)
     */
    public boolean getDefaultEnabled() {
        return true;
    }
}
