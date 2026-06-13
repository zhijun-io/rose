package io.zhijun.dev.services.artemis;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ArtemisDevServicesAutoConfiguration}.
 */
class ArtemisDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(
            ArtemisDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return ArtemisDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return RoseArtemisContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "artemis";
    }

    @Test
    void containerAvailableInDevMode() {
        getContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(RoseArtemisContainer.class);
                    RoseArtemisContainer container = context.getBean(RoseArtemisContainer.class);
                    assertThat(container.getDockerImageName()).contains(RoseArtemisContainer.COMPATIBLE_IMAGE_NAME);
                    assertThat(container.getEnv()).anyMatch(env -> env.startsWith("AMQ_USER="));
                    assertThat(container.getNetworkAliases()).hasSize(1);
                    assertThat(container.isShouldBeReused()).isTrue();
                    assertThat(container.getBinds()).isEmpty();
                    container.start();
                    assertThat(container.getUsername()).isEqualTo(ArtemisDevServicesProperties.DEFAULT_USERNAME);
                    assertThat(container.getPassword()).isEqualTo(ArtemisDevServicesProperties.DEFAULT_PASSWORD);
                    container.stop();

                    assertThatHasSingletonScope(context);
                });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(
                commonConfigurationProperties(),
                "rose.dev.services.artemis.username=myusername",
                "rose.dev.services.artemis.password=mypassword");

        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    RoseArtemisContainer container = context.getBean(RoseArtemisContainer.class);
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    assertThat(container.getUsername()).isEqualTo("myusername");
                    assertThat(container.getPassword()).isEqualTo("mypassword");
                    container.stop();
                });
    }
}
