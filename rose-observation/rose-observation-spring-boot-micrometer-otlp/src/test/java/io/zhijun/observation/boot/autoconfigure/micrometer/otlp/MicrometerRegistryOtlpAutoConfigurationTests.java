package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import java.time.Duration;

import io.micrometer.registry.otlp.OtlpMeterRegistry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsConnectionDetails;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link MicrometerRegistryOtlpAutoConfiguration}.
 */
class MicrometerRegistryOtlpAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MetricsAutoConfiguration.class,
                    MicrometerRegistryOtlpAutoConfiguration.class))
            .withBean(OtlpMetricsConnectionDetails.class, () -> protocol ->
                    protocol == Protocol.GRPC ? "http://localhost:4317" : "http://localhost:4318/v1/metrics")
            .withBean(OpenTelemetryExporterProperties.class, OpenTelemetryExporterProperties::new)
            .withBean(OpenTelemetryMetricsExporterProperties.class, OpenTelemetryMetricsExporterProperties::new);

    @Test
    void autoConfigurationNotActivatedWhenRegistryDisabled() {
        contextRunner
                .withPropertyValues("rose.otel.exporter.otlp.micrometer.enabled=false")
                .withBean(Resource.class, Resource::getDefault)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MicrometerOtlpConfig.class);
                    assertThat(context).doesNotHaveBean(OtlpMeterRegistry.class);
                });
    }

    @Test
    void autoConfigurationNotActivatedWhenOpenTelemetryDisabled() {
        contextRunner
                .withPropertyValues("rose.otel.enabled=false")
                .withBean(Resource.class, Resource::getDefault)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MicrometerOtlpConfig.class);
                    assertThat(context).doesNotHaveBean(OtlpMeterRegistry.class);
                });
    }

    @Test
    void autoConfigurationNotActivatedWhenMetricsDisabled() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.enabled=false")
                .withBean(Resource.class, Resource::getDefault)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MicrometerOtlpConfig.class);
                    assertThat(context).doesNotHaveBean(OtlpMeterRegistry.class);
                });
    }

    @Test
    void autoConfigurationNotActivatedWhenMetricsExporterNotOtlp() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.type=console")
                .withBean(Resource.class, Resource::getDefault)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MicrometerOtlpConfig.class);
                    assertThat(context).doesNotHaveBean(OtlpMeterRegistry.class);
                });
    }

    @Test
    void autoConfigurationNotActivatedWhenMetricsExportDisabled() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.type=none")
                .withBean(Resource.class, Resource::getDefault)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MicrometerOtlpConfig.class);
                    assertThat(context).doesNotHaveBean(OtlpMeterRegistry.class);
                });
    }

    @Test
    void beansAvailableWithDefaultConfiguration() {
        contextRunner
                .withBean(Resource.class, Resource::getDefault)
                .run(context -> {
                    assertThat(context).hasSingleBean(MicrometerOtlpConfig.class);
                    assertThat(context).hasSingleBean(OtlpMeterRegistry.class);

                    MicrometerOtlpConfig config = context.getBean(MicrometerOtlpConfig.class);
                    assertThat(config).isNotNull();
                    assertThat(config.url()).isEqualTo("http://localhost:4318/v1/metrics");

                    OtlpMeterRegistry registry = context.getBean(OtlpMeterRegistry.class);
                    assertThat(registry).isNotNull();
                });
    }

    @Test
    void otlpConfigConfiguredWithCustomProperties() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.interval=PT10S")
                .withBean(Resource.class, Resource::getDefault)
                .run(context -> {
                    assertThat(context).hasSingleBean(MicrometerOtlpConfig.class);

                    MicrometerOtlpConfig config = context.getBean(MicrometerOtlpConfig.class);
                    assertThat(config.step()).isEqualTo(Duration.ofSeconds(10));
                });
    }

    @Test
    void otlpConfigWithResourceAttributes() {
        contextRunner
                .withBean(Resource.class, () -> Resource.create(
                        Attributes.of(
                                AttributeKey.stringKey("service.name"), "test-service",
                                AttributeKey.stringKey("service.version"), "1.0.0",
                                AttributeKey.stringKey("telemetry.sdk.language"), "custom",
                                AttributeKey.stringKey("telemetry.sdk.name"), "custom",
                                AttributeKey.stringKey("telemetry.sdk.version"), "2.1.0"
                        )
                ))
                .run(context -> {
                    assertThat(context).hasSingleBean(MicrometerOtlpConfig.class);

                    MicrometerOtlpConfig config = context.getBean(MicrometerOtlpConfig.class);
                    assertThat(config.resourceAttributes()).containsEntry("service.name", "test-service");
                    assertThat(config.resourceAttributes()).containsEntry("service.version", "1.0.0");
                    assertThat(config.resourceAttributes()).doesNotContainKey("telemetry.sdk.language");
                    assertThat(config.resourceAttributes()).doesNotContainKey("telemetry.sdk.name");
                    assertThat(config.resourceAttributes()).doesNotContainKey("telemetry.sdk.version");
                });
    }

}
