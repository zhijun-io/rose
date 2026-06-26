package io.zhijun.devservice.boot.autoconfigure.openlit;

import io.zhijun.devservice.test.BaseDevServiceAutoConfigurationIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link OpenLitDevServicesAutoConfiguration}.
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
        return OpenLitContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "openlit";
    }

    @Test
    void containerAvailableInDevMode() {
        assertContainerAvailableInDevMode(
                OpenLitContainer.class,
                OpenLitContainer.COMPATIBLE_IMAGE_NAME,
                container -> {
                });
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                OpenLitContainer.class,
                commonConfigurationProperties(),
                (context, container) -> {
                    assertThat(context.getEnvironment().getProperty("OTEL_EXPORTER_OTLP_ENDPOINT"))
                            .startsWith("http://");
                    assertThat(context.getEnvironment().getProperty("rose.dev.openlit.ui-url"))
                            .startsWith("http://");
                });
    }
}
