package io.zhijun.devservice.boot.autoconfigure.activemq;

import io.zhijun.devservice.core.api.config.DevServiceCredentials;
import io.zhijun.devservice.test.BaseDevServiceAutoConfigurationIT;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ActiveMqDevServicesAutoConfiguration}.
 */
class ActiveMqDevServiceAutoConfigurationIT extends BaseDevServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner =
            defaultContextRunner(ActiveMqDevServicesAutoConfiguration.class);

    @Override
    protected ApplicationContextRunner getContextRunner() {
        return contextRunner;
    }

    @Override
    protected Class<?> getAutoConfigurationClass() {
        return ActiveMqDevServicesAutoConfiguration.class;
    }

    @Override
    protected Class<?> getContainerClass() {
        return ActiveMqContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "activemq";
    }

    @Test
    void containerAvailableInDevMode() {
        assertContainerAvailableInDevMode(
                ActiveMqContainer.class, ActiveMqContainer.COMPATIBLE_IMAGE_NAME, container -> {
                    assertThat(container.getEnv()).anyMatch(env -> env.startsWith("ACTIVEMQ_CONNECTION_USER="));
                    assertThat(container.getBinds()).isEmpty();
                    assertThat(container.getUsername()).isEqualTo(DevServiceCredentials.DEFAULT_USERNAME);
                    assertThat(container.getPassword()).isEqualTo(DevServiceCredentials.DEFAULT_PASSWORD);
                });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(
                commonConfigurationProperties(),
                "rose.dev.activemq.username=myusername",
                "rose.dev.activemq.password=mypassword");

        assertContainerConfigurationApplied(ActiveMqContainer.class, properties, (context, container) -> {
            assertThat(container.getUsername()).isEqualTo("myusername");
            assertThat(container.getPassword()).isEqualTo("mypassword");
            assertThat(context.getEnvironment().getProperty("spring.activemq.broker-url"))
                    .isNotBlank();
            assertThat(context.getEnvironment().getProperty("spring.activemq.user"))
                    .isEqualTo("myusername");
            assertThat(context.getEnvironment().getProperty("spring.activemq.password"))
                    .isEqualTo("mypassword");
        });
    }
}
