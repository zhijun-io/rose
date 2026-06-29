package io.zhijun.observation.boot.autoconfigure.otel.resource.template;

import java.util.Map;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;

import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceProperties;
import io.zhijun.observation.boot.autoconfigure.otel.resource.contributor.ResourceContributor;

/**
 * Builds and customizes the OpenTelemetry {@link Resource}.
 */
public final class OpenTelemetryResourceTemplate {

    private final OpenTelemetryResourceProperties properties;

    public OpenTelemetryResourceTemplate(OpenTelemetryResourceProperties properties) {
        this.properties = properties;
    }

    public Resource build(
            ObjectProvider<ResourceContributor> resourceContributors,
            ObjectProvider<OpenTelemetryResourceBuilderCustomizer> customizers) {
        ResourceBuilder builder = Resource.getDefault().toBuilder();
        resourceContributors.orderedStream().forEach(contributor -> contributor.contribute(builder));
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    public OpenTelemetryResourceBuilderCustomizer filterAttributes() {
        return builder -> {
            Map<String, Boolean> attributeKeysMap = properties.getEnable();
            Boolean allKeys = attributeKeysMap.get("all");
            if (allKeys == null) {
                attributeKeysMap.forEach((prefix, enabled) -> {
                    if (!enabled) {
                        removeAttributesWithPrefix(builder, prefix);
                    }
                });
                return;
            }
            if (!allKeys) {
                builder.removeIf(attributeKey -> true);
            }
        };
    }

    private static void removeAttributesWithPrefix(ResourceBuilder builder, String prefix) {
        builder.removeIf(attributeKey -> attributeKey.getKey().startsWith(prefix));
    }
}
