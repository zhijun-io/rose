package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.common.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OtlpMetricsConnectionDetails;

class MicrometerRegistryOtlpTemplateTests {

    private final MicrometerRegistryOtlpTemplate template = new MicrometerRegistryOtlpTemplate();

    @Test
    void buildUsesSignalProtocolAndFiltersReservedResourceAttributes() {
        OtlpMetricsConnectionDetails connectionDetails = protocol ->
                protocol == Protocol.GRPC ? "http://localhost:4317" : "http://localhost:4318/v1/metrics";
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        OpenTelemetryMetricsExporterProperties metricsProperties = new OpenTelemetryMetricsExporterProperties();
        metricsProperties.getOtlp().setProtocol(Protocol.GRPC);
        metricsProperties.setInterval(Duration.ofSeconds(10));
        Resource resource = Resource.create(Attributes.of(
                AttributeKey.stringKey("service.name"), "test-service",
                AttributeKey.stringKey("telemetry.sdk.language"), "custom"));

        MicrometerOtlpConfig config = template.build(connectionDetails, commonProperties, metricsProperties, resource);

        assertThat(config.url()).isEqualTo("http://localhost:4317");
        assertThat(config.step()).isEqualTo(Duration.ofSeconds(10));
        assertThat(config.resourceAttributes()).containsEntry("service.name", "test-service");
        assertThat(config.resourceAttributes()).doesNotContainKey("telemetry.sdk.language");
    }
}
