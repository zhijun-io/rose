package io.zhijun.devservice.boot.autoconfigure.artemis;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.api.config.DevServiceCredentials;
import io.zhijun.devservice.test.BaseDevServicePropertiesTests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ArtemisDevServiceProperties}.
 */
class ArtemisDevServicePropertiesTests extends BaseDevServicePropertiesTests<ArtemisDevServiceProperties> {

    @Override
    protected ArtemisDevServiceProperties createProperties() {
        return new ArtemisDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(ArtemisDevServiceProperties.DEFAULT_IMAGE_NAME)
                .shared(true)
                .startupTimeout(BaseDevServiceProperties.SLOW_STARTUP_TIMEOUT)
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        ArtemisDevServiceProperties properties = createProperties();

        assertThat(properties.getManagementConsolePort()).isEqualTo(BaseDevServiceProperties.RANDOM_PORT);
        assertThat(properties.getUsername()).isEqualTo(DevServiceCredentials.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(DevServiceCredentials.DEFAULT_PASSWORD);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        ArtemisDevServiceProperties properties = createProperties();

        properties.setManagementConsolePort(ArtemisContainer.WEB_CONSOLE_PORT);
        properties.setUsername("myusername");
        properties.setPassword("mypassword");

        assertThat(properties.getManagementConsolePort()).isEqualTo(ArtemisContainer.WEB_CONSOLE_PORT);
        assertThat(properties.getUsername()).isEqualTo("myusername");
        assertThat(properties.getPassword()).isEqualTo("mypassword");
    }
}
