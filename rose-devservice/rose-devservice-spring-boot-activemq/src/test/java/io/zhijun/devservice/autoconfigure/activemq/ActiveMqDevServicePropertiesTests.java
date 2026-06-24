package io.zhijun.devservice.autoconfigure.activemq;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ActiveMqDevServiceProperties}.
 */
class ActiveMqDevServicePropertiesTests extends BaseDevServicePropertiesTests<ActiveMqDevServiceProperties> {

    @Override
    protected ActiveMqDevServiceProperties createProperties() {
        return new ActiveMqDevServiceProperties();
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
        ActiveMqDevServiceProperties properties = createProperties();

        assertThat(properties.getManagementConsolePort()).isZero();
        assertThat(properties.getUsername()).isEqualTo(ActiveMqDevServiceProperties.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(ActiveMqDevServiceProperties.DEFAULT_PASSWORD);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        ActiveMqDevServiceProperties properties = createProperties();

        properties.setManagementConsolePort(RoseActiveMqContainer.WEB_CONSOLE_PORT);
        properties.setUsername("myusername");
        properties.setPassword("mypassword");

        assertThat(properties.getManagementConsolePort()).isEqualTo(RoseActiveMqContainer.WEB_CONSOLE_PORT);
        assertThat(properties.getUsername()).isEqualTo("myusername");
        assertThat(properties.getPassword()).isEqualTo("mypassword");
    }

}
