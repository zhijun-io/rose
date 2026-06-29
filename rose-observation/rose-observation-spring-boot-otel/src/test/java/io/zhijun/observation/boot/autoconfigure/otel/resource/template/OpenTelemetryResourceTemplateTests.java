package io.zhijun.observation.boot.autoconfigure.otel.resource.template;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceProperties;

/**
 * Unit test for {@link OpenTelemetryResourceTemplate}.
 */
class OpenTelemetryResourceTemplateTests {

    @Test
    void shouldRemoveAttributesByDisabledPrefix() {
        OpenTelemetryResourceProperties properties = new OpenTelemetryResourceProperties();
        properties.getEnable().put("host", false);
        properties.getEnable().put("process.pid", false);

        OpenTelemetryResourceBuilderCustomizer customizer =
                new OpenTelemetryResourceTemplate(properties).filterAttributes();

        ResourceBuilder builder = Resource.getDefault().toBuilder();
        builder.put("host.name", "test-host");
        builder.put("host.id", "test-id");
        builder.put("process.pid", "123");
        builder.put("process.user", "johnny");
        builder.put("service.name", "test-service");

        customizer.customize(builder);
        Resource resource = builder.build();

        assertThat(resource.getAttribute(AttributeKey.stringKey("host.name"))).isNull();
        assertThat(resource.getAttribute(AttributeKey.stringKey("host.id"))).isNull();
        assertThat(resource.getAttribute(AttributeKey.stringKey("process.pid"))).isNull();
        assertThat(resource.getAttribute(AttributeKey.stringKey("process.user"))).isEqualTo("johnny");
        assertThat(resource.getAttribute(AttributeKey.stringKey("service.name"))).isEqualTo("test-service");
    }

    @Test
    void shouldRemoveAllAttributesWhenAllIsDisabled() {
        OpenTelemetryResourceProperties properties = new OpenTelemetryResourceProperties();
        properties.getEnable().put("all", false);

        OpenTelemetryResourceBuilderCustomizer customizer =
                new OpenTelemetryResourceTemplate(properties).filterAttributes();

        ResourceBuilder builder = Resource.getDefault().toBuilder();
        builder.put("host.name", "test-host");
        builder.put("service.name", "test-service");

        customizer.customize(builder);
        Resource resource = builder.build();

        assertThat(resource.getAttribute(AttributeKey.stringKey("host.name"))).isNull();
        assertThat(resource.getAttribute(AttributeKey.stringKey("service.name"))).isNull();
    }
}
