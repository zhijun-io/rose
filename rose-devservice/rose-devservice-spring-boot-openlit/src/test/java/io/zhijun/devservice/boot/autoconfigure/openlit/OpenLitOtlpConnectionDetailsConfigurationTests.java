package io.zhijun.devservice.boot.autoconfigure.openlit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.boot.autoconfigure.otel.common.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OtlpMetricsConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OtlpTracingConnectionDetails;

/**
 * Unit test for {@link OpenLitOtlpConnectionDetailsConfiguration}.
 */
class OpenLitOtlpConnectionDetailsConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenLitOtlpConnectionDetailsConfiguration.class));

    @Test
    void registersOtlpConnectionDetailsWhenContainerIsRunning() {
        OpenLitContainer container = mock(OpenLitContainer.class);
        when(container.isRunning()).thenReturn(true);
        when(container.getHost()).thenReturn("localhost");
        when(container.getOtlpHttpPort()).thenReturn(4318);
        when(container.getOtlpGrpcPort()).thenReturn(4317);

        contextRunner.withBean(OpenLitContainer.class, () -> container).run(context -> {
            assertThat(context).hasSingleBean(OtlpTracingConnectionDetails.class);
            assertThat(context).hasSingleBean(OtlpMetricsConnectionDetails.class);

            OtlpTracingConnectionDetails tracing = context.getBean(OtlpTracingConnectionDetails.class);
            OtlpMetricsConnectionDetails metrics = context.getBean(OtlpMetricsConnectionDetails.class);

            assertThat(tracing.getUrl(Protocol.HTTP_PROTOBUF)).isEqualTo("http://localhost:4318/v1/traces");
            assertThat(metrics.getUrl(Protocol.HTTP_PROTOBUF)).isEqualTo("http://localhost:4318/v1/metrics");
        });
    }

    @Test
    void startsContainerWhenNotRunning() {
        OpenLitContainer container = mock(OpenLitContainer.class);
        when(container.isRunning()).thenReturn(false);
        when(container.getHost()).thenReturn("localhost");
        when(container.getOtlpHttpPort()).thenReturn(4318);
        when(container.getOtlpGrpcPort()).thenReturn(4317);

        contextRunner.withBean(OpenLitContainer.class, () -> container).run(context -> {
            assertThat(context).hasSingleBean(OtlpTracingConnectionDetails.class);
            verify(container, atLeastOnce()).start();
        });
    }
}
