package io.zhijun.boot.env.defaults;

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

/**
 * Merges {@code rose/default/*} configuration files from all jars into Spring Boot default properties.
 * <p>
 * Runs before {@code application.yml} is loaded; see {@link DefaultConfigProperties#ENABLED}.
 */
public final class DefaultConfigPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private final DefaultConfigPropertiesLoader loader = new DefaultConfigPropertiesLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Assert.notNull(environment, "environment cannot be null");
        Assert.notNull(application, "application cannot be null");

        Boolean enabled = environment.getProperty(DefaultConfigProperties.ENABLED, Boolean.class, Boolean.TRUE);
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
        Collections.addAll(patterns, DefaultConfigProperties.DEFAULT_LOCATION_PATTERNS);
        String additionalLocations = environment.getProperty(DefaultConfigProperties.LOCATIONS);
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
