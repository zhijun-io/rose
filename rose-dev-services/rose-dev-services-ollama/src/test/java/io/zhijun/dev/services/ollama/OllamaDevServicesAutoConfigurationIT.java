package io.zhijun.dev.services.ollama;

import org.apache.commons.lang3.ArrayUtils;
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
        contextRunner
                .withSystemProperties("rose.bootstrap.mode=dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(getContainerClass());
                    RoseOllamaContainer container = context.getBean(RoseOllamaContainer.class);
                    assertThat(container.getDockerImageName()).contains(RoseOllamaContainer.COMPATIBLE_IMAGE_NAME);
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
                    RoseOllamaContainer container = context.getBean(RoseOllamaContainer.class);
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    assertThat(context.getEnvironment().getProperty(OllamaDevServicesProperties.BASE_URL_PROPERTY))
                            .startsWith("http://");
                    container.stop();
                });
    }
}
