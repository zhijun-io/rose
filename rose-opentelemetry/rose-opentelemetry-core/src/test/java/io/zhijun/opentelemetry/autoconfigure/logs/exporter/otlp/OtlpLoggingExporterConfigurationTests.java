package io.zhijun.opentelemetry.autoconfigure.logs.exporter.otlp;

import io.zhijun.opentelemetry.autoconfigure.exporter.OpenTelemetryExporterAutoConfiguration;
import io.zhijun.opentelemetry.autoconfigure.logs.exporter.OpenTelemetryLoggingExporterAutoConfiguration;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OtlpLoggingExporterConfiguration}.
 */
class OtlpLoggingExporterConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryExporterAutoConfiguration.class,
                OpenTelemetryLoggingExporterAutoConfiguration.class));

    @Test
    void otlpExporterConfigurationEnabledByDefault() {
        contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpLoggingExporterConfiguration.class);
                assertThat(context).hasSingleBean(OtlpLoggingConnectionDetails.class);
                assertThat(context).hasSingleBean(OtlpHttpLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcLogRecordExporter.class);
            });
    }

    @Test
    void otlpExporterConfigurationEnabledWhenTypeIsOtlp() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.type=otlp")
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpLoggingExporterConfiguration.class);
                assertThat(context).hasSingleBean(OtlpLoggingConnectionDetails.class);
                assertThat(context).hasSingleBean(OtlpHttpLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcLogRecordExporter.class);
            });
    }

    @Test
    void otlpExporterConfigurationDisabledWhenTypeIsNotOtlp() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.type=console")
            .run(context -> {
                assertThat(context).doesNotHaveBean(OtlpLoggingExporterConfiguration.class);
                assertThat(context).doesNotHaveBean(OtlpLoggingConnectionDetails.class);
                assertThat(context).doesNotHaveBean(OtlpHttpLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcLogRecordExporter.class);
            });
    }

    @Test
    void httpProtobufExporterCreatedByDefault() {
        contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpHttpLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcLogRecordExporter.class);
            });
    }

    @Test
    void httpProtobufExporterCreatedWhenProtocolIsHttpProtobuf() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.otlp.protocol=http_protobuf")
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpHttpLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcLogRecordExporter.class);
            });
    }

    @Test
    void grpcExporterCreatedWhenProtocolIsGrpc() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.otlp.protocol=grpc")
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpGrpcLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(OtlpHttpLogRecordExporter.class);
            });
    }

    @Test
    void existingConnectionDetailsRespected() {
        contextRunner
            .withUserConfiguration(CustomOtlpLoggingConnectionDetailsConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpLoggingConnectionDetails.class);
                assertThat(context).hasSingleBean(OtlpHttpLogRecordExporter.class);
            });
    }

    @Test
    void existingHttpExporterRespected() {
        contextRunner
            .withUserConfiguration(CustomOtlpHttpLogRecordExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpHttpLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(OtlpGrpcLogRecordExporter.class);
            });
    }

    @Test
    void existingGrpcExporterRespected() {
        contextRunner
            .withPropertyValues("rose.otel.logs.exporter.otlp.protocol=grpc")
            .withUserConfiguration(CustomOtlpGrpcLogRecordExporterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(OtlpGrpcLogRecordExporter.class);
                assertThat(context).doesNotHaveBean(OtlpHttpLogRecordExporter.class);
            });
    }

    @Test
    void commonEndpointWithPathPreservedForHttpProtobuf() {
        contextRunner
            .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://eu.api.smith.langchain.com/otel")
            .run(context -> {
                OtlpLoggingConnectionDetails connectionDetails = context.getBean(OtlpLoggingConnectionDetails.class);
                assertThat(connectionDetails.getUrl(io.zhijun.opentelemetry.autoconfigure.exporter.otlp.Protocol.HTTP_PROTOBUF))
                    .isEqualTo("https://eu.api.smith.langchain.com/otel/v1/logs");
            });
    }

    @Test
    void commonEndpointWithTrailingSlashHandledCorrectly() {
        contextRunner
            .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://example.com/path/")
            .run(context -> {
                OtlpLoggingConnectionDetails connectionDetails = context.getBean(OtlpLoggingConnectionDetails.class);
                assertThat(connectionDetails.getUrl(io.zhijun.opentelemetry.autoconfigure.exporter.otlp.Protocol.HTTP_PROTOBUF))
                    .isEqualTo("https://example.com/path/v1/logs");
            });
    }

    @Test
    void commonEndpointWithoutPathAppendsCorrectly() {
        contextRunner
            .withPropertyValues("rose.otel.exporter.otlp.endpoint=https://example.com")
            .run(context -> {
                OtlpLoggingConnectionDetails connectionDetails = context.getBean(OtlpLoggingConnectionDetails.class);
                assertThat(connectionDetails.getUrl(io.zhijun.opentelemetry.autoconfigure.exporter.otlp.Protocol.HTTP_PROTOBUF))
                    .isEqualTo("https://example.com/v1/logs");
            });
    }

    @Test
    void commonEndpointNotModifiedForGrpc() {
        contextRunner
            .withPropertyValues(
                "rose.otel.exporter.otlp.endpoint=https://example.com/otel",
                "rose.otel.logs.exporter.otlp.protocol=grpc"
            )
            .run(context -> {
                OtlpLoggingConnectionDetails connectionDetails = context.getBean(OtlpLoggingConnectionDetails.class);
                assertThat(connectionDetails.getUrl(io.zhijun.opentelemetry.autoconfigure.exporter.otlp.Protocol.GRPC))
                    .isEqualTo("https://example.com/otel");
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpLoggingConnectionDetailsConfiguration {

        @Bean
        OtlpLoggingConnectionDetails otlpLoggingConnectionDetails() {
            return protocol -> "http://test:4318";
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpHttpLogRecordExporterConfiguration {

        @Bean
        OtlpHttpLogRecordExporter otlpHttpLogRecordExporter() {
            return OtlpHttpLogRecordExporter.builder().build();
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOtlpGrpcLogRecordExporterConfiguration {

        @Bean
        OtlpGrpcLogRecordExporter otlpGrpcLogRecordExporter() {
            return OtlpGrpcLogRecordExporter.builder().build();
        }

    }

}
