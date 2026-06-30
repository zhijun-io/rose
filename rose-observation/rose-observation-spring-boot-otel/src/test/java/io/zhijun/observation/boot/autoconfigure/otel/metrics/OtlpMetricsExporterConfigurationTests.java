package io.zhijun.observation.boot.autoconfigure.otel.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.common.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.support.MetricsTestBeans;

/**
 * Unit test for {@link OtlpMetricsExporterConfiguration}.
 */
class OtlpMetricsExporterConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    OpenTelemetryExporterAutoConfiguration.class, OpenTelemetryMetricsExporterAutoConfiguration.class))
            .withUserConfiguration(MetricsTestBeans.class);

    @Test
    void otlpExporterConfigurationEnabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
            assertThat(context).hasSingleBean(OtlpMetricsConnectionDetails.class);
            assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
            assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
            assertThat(context).hasBean("histogramAggregation");

            // Verify default histogram aggregation
            OpenTelemetryMetricsExporterProperties properties =
                    context.getBean(OpenTelemetryMetricsExporterProperties.class);
            assertThat(properties.getHistogramAggregation().name()).isEqualTo("EXPLICIT_BUCKET_HISTOGRAM");
        });
    }

    @Test
    void otlpExporterConfigurationEnabledWhenTypeIsOtlp() {
        contextRunner.withPropertyValues("rose.otel.metrics.exporter.type=otlp").run(context -> {
            assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
            assertThat(context).hasSingleBean(OtlpMetricsConnectionDetails.class);
            assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
            assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
        });
    }

    @Test
    void otlpExporterConfigurationDisabledWhenTypeIsNotOtlp() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.type=console")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OtlpMetricsExporterConfiguration.class);
                    assertThat(context).doesNotHaveBean(OtlpMetricsConnectionDetails.class);
                    assertThat(context).doesNotHaveBean(OtlpHttpMetricExporter.class);
                    assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
                });
    }

    @Test
    void httpProtobufExporterCreatedByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
            assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
        });
    }

    @Test
    void httpProtobufExporterCreatedWhenProtocolIsHttpProtobuf() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.otlp.protocol=http_protobuf")
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
                    assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
                });
    }

    @Test
    void grpcExporterCreatedWhenProtocolIsGrpc() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.otlp.protocol=grpc")
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpGrpcMetricExporter.class);
                    assertThat(context).doesNotHaveBean(OtlpHttpMetricExporter.class);
                });
    }

    @Test
    void existingConnectionDetailsRespected() {
        contextRunner
                .withUserConfiguration(CustomOtlpMetricsConnectionDetailsConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpMetricsConnectionDetails.class);
                    assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
                });
    }

    @Test
    void aggregationTemporalityConfigurationRespected() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.aggregation-temporality=delta")
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
                });
    }

    @Test
    void compressionConfigurationRespected() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.otlp.compression=gzip")
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
                });
    }

    @Test
    void timeoutConfigurationRespected() {
        contextRunner
                .withPropertyValues(
                        "rose.otel.metrics.exporter.otlp.timeout=5s",
                        "rose.otel.metrics.exporter.otlp.connect-timeout=2s")
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
                });
    }

    @Test
    void headersConfigurationRespected() {
        contextRunner
                .withPropertyValues(
                        "rose.otel.metrics.exporter.otlp.headers.test=value",
                        "rose.otel.exporter.otlp.headers.common=shared")
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
                });
    }

    @Test
    void customEndpointConfigurationRespected() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.otlp.endpoint=http://custom:4318")
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
                    OtlpMetricsConnectionDetails connectionDetails =
                            context.getBean(OtlpMetricsConnectionDetails.class);
                    assertThat(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF)).isEqualTo("http://custom:4318");
                });
    }

    @Test
    void existingHttpExporterRespected() {
        contextRunner
                .withUserConfiguration(CustomOtlpHttpMetricExporterConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
                    assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
                });
    }

    @Test
    void existingGrpcExporterRespected() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.otlp.protocol=grpc")
                .withUserConfiguration(CustomOtlpGrpcMetricExporterConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpGrpcMetricExporter.class);
                    assertThat(context).doesNotHaveBean(OtlpHttpMetricExporter.class);
                });
    }

    @Test
    void histogramAggregationConfigurationApplied() {
        contextRunner
                .withPropertyValues(
                        "rose.otel.metrics.exporter.histogram-aggregation=base2-exponential-bucket-histogram")
                .run(context -> {
                    assertThat(context)
                            .getBeanNames(OpenTelemetryMeterProviderBuilderCustomizer.class)
                            .hasSize(2);
                    assertThat(context).hasBean("histogramAggregation");
                    assertThat(context).hasBean("metricReaderCustomizer");

                    // Verify the histogram aggregation property is set correctly
                    OpenTelemetryMetricsExporterProperties properties =
                            context.getBean(OpenTelemetryMetricsExporterProperties.class);
                    assertThat(properties.getHistogramAggregation().name())
                            .isEqualTo("BASE2_EXPONENTIAL_BUCKET_HISTOGRAM");
                });

        contextRunner
                .withPropertyValues("rose.otel.metrics.exporter.histogram-aggregation=explicit-bucket-histogram")
                .run(context -> {
                    assertThat(context)
                            .getBeanNames(OpenTelemetryMeterProviderBuilderCustomizer.class)
                            .hasSize(2);
                    assertThat(context).hasBean("histogramAggregation");
                    assertThat(context).hasBean("metricReaderCustomizer");

                    // Verify the histogram aggregation property is set correctly
                    OpenTelemetryMetricsExporterProperties properties =
                            context.getBean(OpenTelemetryMetricsExporterProperties.class);
                    assertThat(properties.getHistogramAggregation().name()).isEqualTo("EXPLICIT_BUCKET_HISTOGRAM");
                });
    }

    @Test
    void commonEndpointWithPathPreservedForHttpProtobuf() {
        contextRunner
                .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://eu.api.smith.langchain.com/otel")
                .run(context -> {
                    OtlpMetricsConnectionDetails connectionDetails =
                            context.getBean(OtlpMetricsConnectionDetails.class);
                    assertThat(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                            .isEqualTo("https://eu.api.smith.langchain.com/otel/v1/metrics");
                });
    }

    @Test
    void commonEndpointWithTrailingSlashHandledCorrectly() {
        contextRunner
                .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://example.com/path/")
                .run(context -> {
                    OtlpMetricsConnectionDetails connectionDetails =
                            context.getBean(OtlpMetricsConnectionDetails.class);
                    assertThat(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                            .isEqualTo("https://example.com/path/v1/metrics");
                });
    }

    @Test
    void commonEndpointWithoutPathAppendsCorrectly() {
        contextRunner
                .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://example.com")
                .run(context -> {
                    OtlpMetricsConnectionDetails connectionDetails =
                            context.getBean(OtlpMetricsConnectionDetails.class);
                    assertThat(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                            .isEqualTo("https://example.com/v1/metrics");
                });
    }

    @Test
    void commonEndpointNotModifiedForGrpc() {
        contextRunner
                .withPropertyValues(
                        "rose.otel.exporter.otlp.endpoint=https://example.com/otel",
                        "rose.otel.metrics.exporter.otlp.protocol=grpc")
                .run(context -> {
                    OtlpMetricsConnectionDetails connectionDetails =
                            context.getBean(OtlpMetricsConnectionDetails.class);
                    assertThat(connectionDetails.getUrl(Protocol.GRPC)).isEqualTo("https://example.com/otel");
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpMetricsConnectionDetailsConfiguration {

        @Bean
        OtlpMetricsConnectionDetails otlpMetricsConnectionDetails() {
            return new OtlpMetricsConnectionDetails() {
                @Override
                public String getUrl(Protocol protocol) {
                    return "http://test:4318";
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpHttpMetricExporterConfiguration {

        @Bean
        OtlpHttpMetricExporter otlpHttpMetricExporter() {
            return OtlpHttpMetricExporter.builder().build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpGrpcMetricExporterConfiguration {

        @Bean
        OtlpGrpcMetricExporter otlpGrpcMetricExporter() {
            return OtlpGrpcMetricExporter.builder().build();
        }
    }
}
