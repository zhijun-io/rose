package io.zhijun.local.artemis;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.local.tests.BaseLocalServicePropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ArtemisLocalServiceProperties}.
 */
class ArtemisDevServicePropertiesTests extends BaseLocalServicePropertiesTests<ArtemisLocalServiceProperties> {

    @Override
    protected ArtemisLocalServiceProperties createProperties() {
        return new ArtemisLocalServiceProperties();
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
        ArtemisLocalServiceProperties properties = createProperties();

        assertThat(properties.getManagementConsolePort()).isEqualTo(0);
        assertThat(properties.getUsername()).isEqualTo(ArtemisLocalServiceProperties.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(ArtemisLocalServiceProperties.DEFAULT_PASSWORD);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        ArtemisLocalServiceProperties properties = createProperties();

        properties.setManagementConsolePort(RoseArtemisContainer.WEB_CONSOLE_PORT);
        properties.setUsername("myusername");
        properties.setPassword("mypassword");

        assertThat(properties.getManagementConsolePort()).isEqualTo(RoseArtemisContainer.WEB_CONSOLE_PORT);
        assertThat(properties.getUsername()).isEqualTo("myusername");
        assertThat(properties.getPassword()).isEqualTo("mypassword");
    }

}
