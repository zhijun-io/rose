package io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.OpenTelemetryTracingExporterAutoConfiguration;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OtlpTracingExporterConfiguration}.
 */
class OtlpTracingExporterConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryExporterAutoConfiguration.class,
                OpenTelemetryTracingExporterAutoConfiguration.class));

    @Test
    void otlpExporterConfigurationEnabledByDefault() {
        contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpTracingExporterConfiguration.class);
                assertThat(context).hasSingleBean(OtlpTracingConnectionDetails.class);
                assertThat(context).hasSingleBean(OtlpHttpSpanExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcSpanExporter.class);
            });
    }

    @Test
    void otlpExporterConfigurationEnabledWhenTypeIsOtlp() {
        contextRunner
            .withPropertyValues("rose.otel.traces.exporter.type=otlp")
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpTracingExporterConfiguration.class);
                assertThat(context).hasSingleBean(OtlpTracingConnectionDetails.class);
                assertThat(context).hasSingleBean(OtlpHttpSpanExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcSpanExporter.class);
            });
    }

    @Test
    void otlpExporterConfigurationDisabledWhenTypeIsNotOtlp() {
        contextRunner
            .withPropertyValues("rose.otel.traces.exporter.type=console")
            .run(context -> {
                assertThat(context).doesNotHaveBean(OtlpTracingExporterConfiguration.class);
                assertThat(context).doesNotHaveBean(OtlpTracingConnectionDetails.class);
                assertThat(context).doesNotHaveBean(OtlpHttpSpanExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcSpanExporter.class);
            });
    }

    @Test
    void httpProtobufExporterCreatedByDefault() {
        contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpHttpSpanExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcSpanExporter.class);
            });
    }

    @Test
    void httpProtobufExporterCreatedWhenProtocolIsHttpProtobuf() {
        contextRunner
            .withPropertyValues("rose.otel.traces.exporter.otlp.protocol=http_protobuf")
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpHttpSpanExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcSpanExporter.class);
            });
    }

    @Test
    void grpcExporterCreatedWhenProtocolIsGrpc() {
        contextRunner
            .withPropertyValues("rose.otel.traces.exporter.otlp.protocol=grpc")
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpGrpcSpanExporter.class);
                assertThat(context).doesNotHaveBean(OtlpHttpSpanExporter.class);
            });
    }

    @Test
    void existingConnectionDetailsRespected() {
        contextRunner
            .withUserConfiguration(CustomOtlpTracingConnectionDetailsConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpTracingConnectionDetails.class);
                assertThat(context).hasSingleBean(OtlpHttpSpanExporter.class);
            });
    }

    @Test
    void existingHttpExporterRespected() {
        contextRunner
            .withUserConfiguration(CustomOtlpHttpSpanExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpHttpSpanExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcSpanExporter.class);
            });
    }

    @Test
    void existingGrpcExporterRespected() {
        contextRunner
            .withPropertyValues("rose.otel.traces.exporter.otlp.protocol=grpc")
            .withUserConfiguration(CustomOtlpGrpcSpanExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpGrpcSpanExporter.class);
                assertThat(context).doesNotHaveBean(OtlpHttpSpanExporter.class);
            });
    }

    @Test
    void commonEndpointWithPathPreservedForHttpProtobuf() {
        contextRunner
            .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://eu.api.smith.langchain.com/otel")
            .run(context -> {
                OtlpTracingConnectionDetails connectionDetails = context.getBean(OtlpTracingConnectionDetails.class);
                assertThat(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                    .isEqualTo("https://eu.api.smith.langchain.com/otel/v1/traces");
            });
    }

    @Test
    void commonEndpointWithTrailingSlashHandledCorrectly() {
        contextRunner
            .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://example.com/path/")
            .run(context -> {
                OtlpTracingConnectionDetails connectionDetails = context.getBean(OtlpTracingConnectionDetails.class);
                assertThat(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                    .isEqualTo("https://example.com/path/v1/traces");
            });
    }

    @Test
    void commonEndpointWithoutPathAppendsCorrectly() {
        contextRunner
            .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://example.com")
            .run(context -> {
                OtlpTracingConnectionDetails connectionDetails = context.getBean(OtlpTracingConnectionDetails.class);
                assertThat(connectionDetails.getUrl(Protocol.HTTP_PROTOBUF))
                    .isEqualTo("https://example.com/v1/traces");
            });
    }

    @Test
    void commonEndpointNotModifiedForGrpc() {
        contextRunner
            .withPropertyValues(
                "rose.otel.exporter.otlp.endpoint=https://example.com/otel",
                "rose.otel.traces.exporter.otlp.protocol=grpc"
            )
            .run(context -> {
                OtlpTracingConnectionDetails connectionDetails = context.getBean(OtlpTracingConnectionDetails.class);
                assertThat(connectionDetails.getUrl(Protocol.GRPC))
                    .isEqualTo("https://example.com/otel");
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpTracingConnectionDetailsConfiguration {

        @Bean
        OtlpTracingConnectionDetails otlpTracingConnectionDetails() {
            return protocol -> "http://test:4318";
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpHttpSpanExporterConfiguration {

        @Bean
        OtlpHttpSpanExporter otlpHttpSpanExporter() {
            return OtlpHttpSpanExporter.builder().build();
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpGrpcSpanExporterConfiguration {

        @Bean
        OtlpGrpcSpanExporter otlpGrpcSpanExporter() {
            return OtlpGrpcSpanExporter.builder().build();
        }

    }

}
