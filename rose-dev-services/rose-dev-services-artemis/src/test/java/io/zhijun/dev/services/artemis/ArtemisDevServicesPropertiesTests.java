package io.zhijun.dev.services.artemis;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesPropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ArtemisDevServicesProperties}.
 */
class ArtemisDevServicesPropertiesTests extends BaseDevServicesPropertiesTests<ArtemisDevServicesProperties> {

    @Override
    protected ArtemisDevServicesProperties createProperties() {
        return new ArtemisDevServicesProperties();
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
        ArtemisDevServicesProperties properties = createProperties();

        assertThat(properties.getManagementConsolePort()).isEqualTo(0);
        assertThat(properties.getUsername()).isEqualTo(ArtemisDevServicesProperties.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(ArtemisDevServicesProperties.DEFAULT_PASSWORD);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        ArtemisDevServicesProperties properties = createProperties();

        properties.setManagementConsolePort(RoseArtemisContainer.WEB_CONSOLE_PORT);
        properties.setUsername("myusername");
        properties.setPassword("mypassword");

        assertThat(properties.getManagementConsolePort()).isEqualTo(RoseArtemisContainer.WEB_CONSOLE_PORT);
        assertThat(properties.getUsername()).isEqualTo("myusername");
        assertThat(properties.getPassword()).isEqualTo("mypassword");
    }

}
