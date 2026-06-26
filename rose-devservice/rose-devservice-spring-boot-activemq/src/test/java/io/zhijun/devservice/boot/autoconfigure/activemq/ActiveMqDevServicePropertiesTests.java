package io.zhijun.devservice.boot.autoconfigure.activemq;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.api.config.DevServiceCredentials;
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
                .imageName(ActiveMqDevServiceProperties.DEFAULT_IMAGE_NAME)
                .shared(true)
                .startupTimeout(BaseDevServiceProperties.SLOW_STARTUP_TIMEOUT)
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        ActiveMqDevServiceProperties properties = createProperties();

        assertThat(properties.getManagementConsolePort()).isEqualTo(BaseDevServiceProperties.RANDOM_PORT);
        assertThat(properties.getUsername()).isEqualTo(DevServiceCredentials.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(DevServiceCredentials.DEFAULT_PASSWORD);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        ActiveMqDevServiceProperties properties = createProperties();

        properties.setManagementConsolePort(ActiveMqContainer.WEB_CONSOLE_PORT);
        properties.setUsername("myusername");
        properties.setPassword("mypassword");

        assertThat(properties.getManagementConsolePort()).isEqualTo(ActiveMqContainer.WEB_CONSOLE_PORT);
        assertThat(properties.getUsername()).isEqualTo("myusername");
        assertThat(properties.getPassword()).isEqualTo("mypassword");
    }

}
