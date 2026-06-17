package io.zhijun.dev.services.ollama;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OllamaDevServicesAutoConfiguration}.
 */
class OllamaDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(OllamaDevServicesAutoConfiguration.class)
            .withPropertyValues("rose.dev.services.ollama.ignore-native-service=true");

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
        return RoseOllamaContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "ollama";
    }

    @Test
    void containerActivatedWhenEnabled() {
        assertContainerAvailableInDevMode(
                RoseOllamaContainer.class,
                RoseOllamaContainer.COMPATIBLE_IMAGE_NAME,
                container -> assertThat(container.getEnv()).isEmpty());
    }

    @Test
    void containerConfigurationApplied() {
        assertContainerConfigurationApplied(
                RoseOllamaContainer.class,
                commonConfigurationProperties(),
                (context, container) -> assertThat(
                        context.getEnvironment().getProperty(OllamaDevServicesProperties.BASE_URL_PROPERTY))
                        .startsWith("http://"));
    }
}
