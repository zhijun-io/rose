package io.zhijun.dev.artemis;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.test.BaseDevServiceAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ArtemisDevServicesAutoConfiguration}.
 */
class ArtemisDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

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
        assertContainerAvailableInDevMode(
                RoseArtemisContainer.class,
                RoseArtemisContainer.COMPATIBLE_IMAGE_NAME,
                container -> {
                    assertThat(container.getEnv()).anyMatch(env -> env.startsWith("AMQ_USER="));
                    assertThat(container.getBinds()).isEmpty();
                    assertThat(container.getUsername()).isEqualTo(ArtemisDevServiceProperties.DEFAULT_USERNAME);
                    assertThat(container.getPassword()).isEqualTo(ArtemisDevServiceProperties.DEFAULT_PASSWORD);
                });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(
                commonConfigurationProperties(),
                "rose.dev.artemis.username=myusername",
                "rose.dev.artemis.password=mypassword");

        assertContainerConfigurationApplied(RoseArtemisContainer.class, properties, (context, container) -> {
            assertThat(container.getUsername()).isEqualTo("myusername");
            assertThat(container.getPassword()).isEqualTo("mypassword");
            assertThat(context.getEnvironment().getProperty("spring.artemis.mode")).isEqualTo("native");
            assertThat(context.getEnvironment().getProperty("spring.artemis.broker-url")).isNotBlank();
            assertThat(context.getEnvironment().getProperty("spring.artemis.user")).isEqualTo("myusername");
            assertThat(context.getEnvironment().getProperty("spring.artemis.password")).isEqualTo("mypassword");
        });
    }
}
