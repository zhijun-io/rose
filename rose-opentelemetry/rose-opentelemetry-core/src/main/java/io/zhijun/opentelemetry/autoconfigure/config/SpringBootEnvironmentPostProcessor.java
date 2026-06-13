package io.zhijun.opentelemetry.autoconfigure.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Disables Spring Boot Actuator OTLP metrics export auto-configuration when Rose OTel is used.
 */
class SpringBootEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "rose-opentelemetry-spring-boot";

    private static final String SPRING_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Assert.notNull(environment, "environment cannot be null");

        Map<String, Object> properties = new HashMap<String, Object>();
        setExcludedAutoConfigurations(getAutoConfigurations(), environment, properties);

        MapPropertySource mapPropertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, properties);
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        mutablePropertySources.addFirst(mapPropertySource);
    }

    private static List<String> getAutoConfigurations() {
        List<String> configurations = new ArrayList<String>();
        configurations.add("org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration");
        return configurations;
    }

    static void setExcludedAutoConfigurations(List<String> autoConfigurations, ConfigurableEnvironment environment,
            Map<String, Object> properties) {
        String value = environment.getProperty(SPRING_AUTOCONFIGURE_EXCLUDE);
        String additionalValue = String.join(",", autoConfigurations);

        if (StringUtils.hasText(value)) {
            properties.put(SPRING_AUTOCONFIGURE_EXCLUDE, value + "," + additionalValue);
        } else {
            properties.put(SPRING_AUTOCONFIGURE_EXCLUDE, additionalValue);
        }
    }
}
