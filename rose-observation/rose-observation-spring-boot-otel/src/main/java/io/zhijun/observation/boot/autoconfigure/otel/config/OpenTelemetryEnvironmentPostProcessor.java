package io.zhijun.observation.boot.autoconfigure.otel.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.Assert;

import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryProperties;

/**
 * Maps OpenTelemetry Environment Variable Specification properties to {@code rose.otel.*}.
 */
public class OpenTelemetryEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPERTY_SOURCE_NAME = "opentelemetry-environment-variable-specification";

    static final String COMPATIBILITY_PROPERTY = OpenTelemetryProperties.COMPATIBILITY_ENV_VAR_SPEC_PROPERTY;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Assert.notNull(environment, "environment cannot be null");

        Boolean enabled = environment.getProperty(COMPATIBILITY_PROPERTY, Boolean.class, true);
        if (!enabled) {
            return;
        }

        Map<String, Object> roseProperties = new HashMap<String, Object>();
        roseProperties.putAll(OpenTelemetryEnvironmentPropertyAdapters.general(environment));
        roseProperties.putAll(OpenTelemetryEnvironmentPropertyAdapters.batchSpanProcessor(environment));
        roseProperties.putAll(OpenTelemetryEnvironmentPropertyAdapters.attributeLimits(environment));
        roseProperties.putAll(OpenTelemetryEnvironmentPropertyAdapters.spanLimits(environment));
        roseProperties.putAll(OpenTelemetryEnvironmentPropertyAdapters.exporterSelection(environment));
        roseProperties.putAll(OpenTelemetryEnvironmentPropertyAdapters.metrics(environment));
        roseProperties.putAll(OpenTelemetryEnvironmentPropertyAdapters.otlpExporter(environment));

        MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, roseProperties);
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(propertySource);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
