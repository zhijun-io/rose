package io.zhijun.spring.boot.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.DefaultPropertiesPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.zhijun.spring.boot.constants.PropertyConstants;

/**
 * Merges {@code config/default/*} and {@code META-INF/config/default/*} configuration files from all jars
 * into Spring Boot default properties.
 * <p>
 * Runs before {@code application.yml} is loaded; disable with {@code -Drose.default-config.enabled=false}
 * or {@code ROSE_DEFAULT_CONFIG_ENABLED=false}.
 */
public final class DefaultConfigPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String ENABLED_PROPERTY = PropertyConstants.DEFAULT_CONFIG_ENABLED_PROPERTY_NAME;

    public static final String LOCATIONS_PROPERTY = PropertyConstants.DEFAULT_CONFIG_LOCATIONS_PROPERTY_NAME;

    public static final String DEFAULT_PROPERTIES_PATTERN = "classpath*:config/default/*.properties";

    public static final String DEFAULT_YML_PATTERN = "classpath*:config/default/*.yml";

    public static final String DEFAULT_YAML_PATTERN = "classpath*:config/default/*.yaml";

    public static final String META_INF_DEFAULT_PROPERTIES_PATTERN = "classpath*:META-INF/config/default/*.properties";

    public static final String META_INF_DEFAULT_YML_PATTERN = "classpath*:META-INF/config/default/*.yml";

    public static final String META_INF_DEFAULT_YAML_PATTERN = "classpath*:META-INF/config/default/*.yaml";

    public static final String[] DEFAULT_LOCATION_PATTERNS = {
        DEFAULT_PROPERTIES_PATTERN,
        DEFAULT_YML_PATTERN,
        DEFAULT_YAML_PATTERN,
        META_INF_DEFAULT_PROPERTIES_PATTERN,
        META_INF_DEFAULT_YML_PATTERN,
        META_INF_DEFAULT_YAML_PATTERN
    };

    private final DefaultConfigPropertiesLoader loader = new DefaultConfigPropertiesLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Assert.notNull(environment, "environment cannot be null");
        Assert.notNull(application, "application cannot be null");

        Boolean enabled = environment.getProperty(ENABLED_PROPERTY, Boolean.class, Boolean.TRUE);
        if (!enabled.booleanValue()) {
            return;
        }

        Map<String, Object> properties = loader.load(resolveLocationPatterns(environment));
        if (properties.isEmpty()) {
            return;
        }
        DefaultPropertiesPropertySource.addOrMerge(properties, environment.getPropertySources());
    }

    private static String[] resolveLocationPatterns(ConfigurableEnvironment environment) {
        List<String> patterns = new ArrayList<String>();
        Collections.addAll(patterns, DEFAULT_LOCATION_PATTERNS);
        String additionalLocations = environment.getProperty(LOCATIONS_PROPERTY);
        if (StringUtils.hasText(additionalLocations)) {
            for (String location : StringUtils.commaDelimitedListToStringArray(additionalLocations)) {
                if (StringUtils.hasText(location)) {
                    patterns.add(location.trim());
                }
            }
        }
        return patterns.toArray(new String[patterns.size()]);
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER - 5;
    }
}
