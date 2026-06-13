package io.zhijun.dev.services.activemq;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.services.tests.BaseDevServicesAutoConfigurationIT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ActiveMqDevServicesAutoConfiguration}.
 */
class ActiveMqDevServicesAutoConfigurationIT extends BaseDevServicesAutoConfigurationIT {

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
        getContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(RoseActiveMqContainer.class);
                    RoseActiveMqContainer container = context.getBean(RoseActiveMqContainer.class);
                    assertThat(container.getDockerImageName()).contains(RoseActiveMqContainer.COMPATIBLE_IMAGE_NAME);
                    assertThat(container.getEnv()).anyMatch(env -> env.startsWith("ACTIVEMQ_CONNECTION_USER="));
                    assertThat(container.getNetworkAliases()).hasSize(1);
                    assertThat(container.isShouldBeReused()).isTrue();
                    assertThat(container.getBinds()).isEmpty();
                    container.start();
                    assertThat(container.getUsername()).isEqualTo(ActiveMqDevServicesProperties.DEFAULT_USERNAME);
                    assertThat(container.getPassword()).isEqualTo(ActiveMqDevServicesProperties.DEFAULT_PASSWORD);
                    container.stop();

                    assertThatHasSingletonScope(context);
                });
    }

    @Test
    void containerConfigurationApplied() {
        String[] properties = ArrayUtils.addAll(
                commonConfigurationProperties(),
                "rose.dev.services.activemq.username=myusername",
                "rose.dev.services.activemq.password=mypassword");

        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    RoseActiveMqContainer container = context.getBean(RoseActiveMqContainer.class);
                    container.start();
                    assertThatConfigurationIsApplied(container);
                    assertThat(container.getUsername()).isEqualTo("myusername");
                    assertThat(container.getPassword()).isEqualTo("mypassword");
                    container.stop();
                });
    }
}
