package io.zhijun.devservice.boot.autoconfigure.ollama;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.devservice.test.BaseDevServiceAutoConfigurationIT;

/**
 * Integration test for {@link OllamaDevServicesAutoConfiguration}.
 */
class OllamaDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(
                    OllamaDevServicesAutoConfiguration.class)
            .withPropertyValues("rose.dev.ollama.ignore-native-service=true");

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return OllamaDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return OllamaContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "ollama";
    }

    @Test
    void containerActivatedWhenEnabled() {
        assertContainerAvailableInDevMode(
                OllamaContainer.class,
                OllamaContainer.COMPATIBLE_IMAGE_NAME,
                container -> assertThat(container.getEnv()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                OllamaContainer.class,
                commonConfigurationProperties(),
                (context, container) -> assertThat(
                                context.getEnvironment().getProperty(OllamaDevServiceProperties.BASE_URL_PROPERTY))
                        .startsWith("http://"));
    }
}
