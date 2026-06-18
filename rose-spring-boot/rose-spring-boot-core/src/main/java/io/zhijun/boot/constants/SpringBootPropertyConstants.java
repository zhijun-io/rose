package io.zhijun.boot.constants;

import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * Constants for Spring Boot configuration properties.
 */
public interface SpringBootPropertyConstants {

    /**
     * Spring Boot property to exclude auto-configuration classes.
     *
     * @see EnableAutoConfiguration
     * @see AutoConfigurationImportSelector#PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE
     */
    String SPRING_AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME = "spring.autoconfigure.exclude";

    /**
     * Name of the {@link PropertySource} attached by {@link ConfigurationPropertySources#attach(Environment)}.
     *
     * @see ConfigurationPropertySources#ATTACHED_PROPERTY_SOURCE_NAME
     */
    String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";

}
