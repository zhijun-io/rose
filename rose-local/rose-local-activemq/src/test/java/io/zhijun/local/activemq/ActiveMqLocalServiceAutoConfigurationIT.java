package io.zhijun.local.activemq;

import io.zhijun.local.tests.BaseLocalServiceAutoConfigurationIT;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ActiveMqDevServicesAutoConfiguration}.
 */
class ActiveMqLocalServiceAutoConfigurationIT extends BaseLocalServiceAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = defaultContextRunner(
            ActiveMqDevServicesAutoConfiguration.class);

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
        return RoseActiveMqContainer.class;
    }

    @Override
    protected String getServiceName() {
        return "activemq";
    }

    @Test
    void containerAvailableInDevMode() {
        assertContainerAvailableInDevMode(
                RoseActiveMqContainer.class,
                RoseActiveMqContainer.COMPATIBLE_IMAGE_NAME,
                container -> {
                    assertThat(container.getEnv()).anyMatch(env -> env.startsWith("ACTIVEMQ_CONNECTION_USER="));
                    assertThat(container.getBinds()).isEmpty();
                    assertThat(container.getUsername()).isEqualTo(ActiveMqLocalServiceProperties.DEFAULT_USERNAME);
                    assertThat(container.getPassword()).isEqualTo(ActiveMqLocalServiceProperties.DEFAULT_PASSWORD);
                });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(
                commonConfigurationProperties(),
                "rose.local.activemq.username=myusername",
                "rose.local.activemq.password=mypassword");

        assertContainerConfigurationApplied(RoseActiveMqContainer.class, properties, (context, container) -> {
            assertThat(container.getUsername()).isEqualTo("myusername");
            assertThat(container.getPassword()).isEqualTo("mypassword");
            assertThat(context.getEnvironment().getProperty("spring.activemq.broker-url")).isNotBlank();
            assertThat(context.getEnvironment().getProperty("spring.activemq.user")).isEqualTo("myusername");
            assertThat(context.getEnvironment().getProperty("spring.activemq.password")).isEqualTo("mypassword");
        });
    }
}
