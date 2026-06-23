package io.zhijun.dev.openlit;

import io.zhijun.dev.tests.BaseDevServiceAutoConfigurationIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OpenLitDevServicesAutoConfiguration}.
 */
class OpenLitDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(OpenLitDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return OpenLitDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return RoseOpenLitContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "openlit";
    }

    @Test
    void containerAvailableInDevMode() {
        assertContainerAvailableInDevMode(
                RoseOpenLitContainer.class,
                RoseOpenLitContainer.COMPATIBLE_IMAGE_NAME,
                container -> {
                });
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                RoseOpenLitContainer.class,
                commonConfigurationProperties(),
                (context, container) -> {
                    assertThat(context.getEnvironment().getProperty("OTEL_EXPORTER_OTLP_ENDPOINT"))
                            .startsWith("http://");
                    assertThat(context.getEnvironment().getProperty("rose.dev.openlit.ui-url"))
                            .startsWith("http://");
                });
    }
}
