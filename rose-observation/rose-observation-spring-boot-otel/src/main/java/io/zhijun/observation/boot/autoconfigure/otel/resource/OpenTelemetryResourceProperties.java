package io.zhijun.observation.boot.autoconfigure.otel.resource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import io.zhijun.core.annotation.Nullable;

/**
 * Configuration properties for OpenTelemetry Resource.
 */
@ConfigurationProperties(prefix = OpenTelemetryResourceProperties.CONFIG_PREFIX)
public class OpenTelemetryResourceProperties {

    public static final String CONFIG_PREFIX = "rose.otel.resource";

    public static final String ATTRIBUTES_PROPERTY = CONFIG_PREFIX + ".attributes";

    public static final String SERVICE_NAME_PROPERTY = CONFIG_PREFIX + ".service-name";

    public static final String CONTRIBUTOR_ENABLED_PROPERTY = CONFIG_PREFIX + ".contributors.%s.enabled";

    /**
     * Name identifying the service.
     */
    @Nullable
    private String serviceName;

    /**
     * Additional attributes to include in the resource.
     */
    private final Map<String, String> attributes = new HashMap<>();

    /**
     * Whether resource attributes having keys starting with the specified name should be enabled.
     * The longest match wins. The key {@code all} can be used to enable/disable all attributes.
     */
    private final Map<String, Boolean> enable = new LinkedHashMap<>();

    @Nullable
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Map<String, Boolean> getEnable() {
        return enable;
    }
}
