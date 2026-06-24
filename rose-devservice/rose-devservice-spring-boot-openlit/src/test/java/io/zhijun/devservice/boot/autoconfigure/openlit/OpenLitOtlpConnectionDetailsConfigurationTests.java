package io.zhijun.devservice.boot.autoconfigure.openlit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp.OtlpTracingConnectionDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link OpenLitOtlpConnectionDetailsConfiguration}.
 */
class OpenLitOtlpConnectionDetailsConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenLitOtlpConnectionDetailsConfiguration.class));

    @Test
    void registersOtlpConnectionDetailsWhenContainerIsRunning() {
        RoseOpenLitContainer container = mock(RoseOpenLitContainer.class);
        when(container.isRunning()).thenReturn(true);
        when(container.getHost()).thenReturn("localhost");
        when(container.getOtlpHttpPort()).thenReturn(4318);
        when(container.getOtlpGrpcPort()).thenReturn(4317);

        contextRunner
                .withBean(RoseOpenLitContainer.class, () -> container)
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpTracingConnectionDetails.class);
                    assertThat(context).hasSingleBean(OtlpMetricsConnectionDetails.class);

                    OtlpTracingConnectionDetails tracing = context.getBean(OtlpTracingConnectionDetails.class);
                    OtlpMetricsConnectionDetails metrics = context.getBean(OtlpMetricsConnectionDetails.class);

                    assertThat(tracing.getUrl(Protocol.HTTP_PROTOBUF))
                            .isEqualTo("http://localhost:4318/v1/traces");
                    assertThat(metrics.getUrl(Protocol.HTTP_PROTOBUF))
                            .isEqualTo("http://localhost:4318/v1/metrics");
                });
    }

    @Test
    void startsContainerWhenNotRunning() {
        RoseOpenLitContainer container = mock(RoseOpenLitContainer.class);
        when(container.isRunning()).thenReturn(false);
        when(container.getHost()).thenReturn("localhost");
        when(container.getOtlpHttpPort()).thenReturn(4318);
        when(container.getOtlpGrpcPort()).thenReturn(4317);

        contextRunner
                .withBean(RoseOpenLitContainer.class, () -> container)
                .run(context -> {
                    assertThat(context).hasSingleBean(OtlpTracingConnectionDetails.class);
                    verify(container, atLeastOnce()).start();
                });
    }

}
