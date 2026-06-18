package io.zhijun.dev.services.opentelemetry.collector;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OtelCollectorDevServicesAutoConfiguration}.
 */
class OtelCollectorDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner =
            defaultContextRunner(OtelCollectorDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return OtelCollectorDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return RoseOtelCollectorContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "otel-collector";
    }

    @Test
    void containerAvailableInDevMode() {
        assertContainerAvailableInDevMode(
                RoseOtelCollectorContainer.class,
                RoseOtelCollectorContainer.COMPATIBLE_IMAGE_NAME,
                container -> {
                    assertThat(container.getEnv()).isEmpty();
                });
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                RoseOtelCollectorContainer.class,
                commonConfigurationProperties(),
                (context, container) -> {
                    assertThat(context.getEnvironment().getProperty("OTEL_EXPORTER_OTLP_ENDPOINT"))
                            .startsWith("http://");
                    assertThat(context.getEnvironment().getProperty("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT"))
                            .startsWith("http://");
                    assertThat(container.getOtlpHttpUrl()).startsWith("http://");
                    assertThat(container.getOtlpGrpcUrl()).startsWith("http://");
                });
    }
}
