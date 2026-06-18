package io.zhijun.local.activemq;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ActiveMqLocalServiceProperties}.
 */
class ActiveMqDevServicePropertiesTests extends BaseLocalServicePropertiesTests<ActiveMqLocalServiceProperties> {

    @Override
    protected ActiveMqLocalServiceProperties createProperties() {
        return new ActiveMqLocalServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseActiveMqContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .startupTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        ActiveMqLocalServiceProperties properties = createProperties();

        assertThat(properties.getManagementConsolePort()).isZero();
        assertThat(properties.getUsername()).isEqualTo(ActiveMqLocalServiceProperties.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(ActiveMqLocalServiceProperties.DEFAULT_PASSWORD);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        ActiveMqLocalServiceProperties properties = createProperties();

        properties.setManagementConsolePort(RoseActiveMqContainer.WEB_CONSOLE_PORT);
        properties.setUsername("myusername");
        properties.setPassword("mypassword");

        assertThat(properties.getManagementConsolePort()).isEqualTo(RoseActiveMqContainer.WEB_CONSOLE_PORT);
        assertThat(properties.getUsername()).isEqualTo("myusername");
        assertThat(properties.getPassword()).isEqualTo("mypassword");
    }

}
