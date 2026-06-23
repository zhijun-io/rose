package io.zhijun.dev.artemis;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.tests.BaseDevServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ArtemisDevServiceProperties}.
 */
class ArtemisDevServicePropertiesTests extends BaseDevServicePropertiesTests<ArtemisDevServiceProperties> {

    @Override
    protected ArtemisDevServiceProperties createProperties() {
        return new ArtemisDevServiceProperties();
    }

    @Override
    protected DefaultValues getExpectedDefaults() {
        return DefaultValues.builder()
                .imageName(RoseArtemisContainer.COMPATIBLE_IMAGE_NAME)
                .shared(true)
                .startupTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Test
    void shouldCreateInstanceWithServiceSpecificDefaultValues() {
        ArtemisDevServiceProperties properties = createProperties();

        assertThat(properties.getManagementConsolePort()).isEqualTo(0);
        assertThat(properties.getUsername()).isEqualTo(ArtemisDevServiceProperties.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(ArtemisDevServiceProperties.DEFAULT_PASSWORD);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        ArtemisDevServiceProperties properties = createProperties();

        properties.setManagementConsolePort(RoseArtemisContainer.WEB_CONSOLE_PORT);
        properties.setUsername("myusername");
        properties.setPassword("mypassword");

        assertThat(properties.getManagementConsolePort()).isEqualTo(RoseArtemisContainer.WEB_CONSOLE_PORT);
        assertThat(properties.getUsername()).isEqualTo("myusername");
        assertThat(properties.getPassword()).isEqualTo("mypassword");
    }

}
