package io.zhijun.spring.constants;

import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import org.springframework.context.ApplicationContextInitializer;

import static java.lang.Boolean.parseBoolean;

/**
 * The Property constants for Rose Spring
 *
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Rose Spring : "rose.spring."
     */
    String ROSE_PROPERTY_NAME_PREFIX = "rose.";

    /**
     * The property name prefix of Rose Spring : "rose.spring."
     */
    String ROSE_SPRING_PROPERTY_NAME_PREFIX = ROSE_PROPERTY_NAME_PREFIX + "spring.";

    /**
     * The property name of enabled : "enabled"
     */
    String ENABLED_PROPERTY_NAME = "enabled";

    /**
     * The char of "@"
     */
    String AT_CHAR = "@";

    /** The char of "." */
    String DOT_CHAR = ".";

    /**
     * The property name prefix of the configuration property prefix : "rose.spring.prefix."
     */
    String PREFIX_PROPERTY_NAME_PREFIX = ROSE_SPRING_PROPERTY_NAME_PREFIX + "prefix.";

    /**
     * The property name prefix of beans : "rose.spring.beans."
     */
    String BEANS_PROPERTY_NAME_PREFIX = ROSE_SPRING_PROPERTY_NAME_PREFIX + "beans.";

    /**
     * The property name prefix of {@link ConfigurableApplicationContextInitializer} : "rose.spring.context-initializer."
     *
     * @see ConfigurableApplicationContextInitializer
     * @see ApplicationContextInitializer
     */
    String APPLICATION_CONTEXT_INITIALIZER_PROPERTY_NAME_PREFIX = ROSE_SPRING_PROPERTY_NAME_PREFIX + "context-initializer.";

    /**
     * The property name suffix of auto registered : "auto-registered"
     */
    String AUTO_REGISTERED_PROPERTY_NAME_SUFFIX = "auto-registered";

    /**
     * The default value of property of auto registered : "true"
     */
    String DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE = "true";

    /**
     * The default value of auto registered : true
     */
    boolean DEFAULT_AUTO_REGISTERED_VALUE = parseBoolean(DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE);
}
