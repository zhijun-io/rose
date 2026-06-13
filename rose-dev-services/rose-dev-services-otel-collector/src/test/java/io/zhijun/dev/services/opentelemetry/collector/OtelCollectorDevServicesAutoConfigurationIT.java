package io.zhijun.dev.services.opentelemetry.collector;

import org.apache.commons.lang3.ArrayUtils;
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
        getContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(getContainerClass());
                    RoseOtelCollectorContainer container = context.getBean(RoseOtelCollectorContainer.class);
                    assertThat(container.getDockerImageName()).contains(RoseOtelCollectorContainer.COMPATIBLE_IMAGE_NAME);
                    assertThat(container.getEnv()).isEmpty();
                    assertThat(container.getNetworkAliases()).hasSize(1);
                    assertThat(container.isShouldBeReused()).isTrue();

                    assertThatHasSingletonScope(context);
                });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(commonConfigurationProperties());

        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    RoseOtelCollectorContainer container = context.getBean(RoseOtelCollectorContainer.class);
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    assertThat(context.getEnvironment().getProperty("OTEL_EXPORTER_OTLP_ENDPOINT"))
                            .startsWith("http://");
                    assertThat(context.getEnvironment().getProperty("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT"))
                            .startsWith("http://");
                    assertThat(container.getOtlpHttpUrl()).startsWith("http://");
                    assertThat(container.getOtlpGrpcUrl()).startsWith("http://");
                    container.stop();
                });
    }
}
