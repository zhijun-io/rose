package io.zhijun.dev.services.activemq;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesPropertiesTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ActiveMqDevServicesProperties}.
 */
class ActiveMqDevServicesPropertiesTests extends BaseDevServicesPropertiesTests<ActiveMqDevServicesProperties> {

    @Override
    protected ActiveMqDevServicesProperties createProperties() {
        return new ActiveMqDevServicesProperties();
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
        ActiveMqDevServicesProperties properties = createProperties();

        assertThat(properties.getManagementConsolePort()).isZero();
        assertThat(properties.getUsername()).isEqualTo(ActiveMqDevServicesProperties.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(ActiveMqDevServicesProperties.DEFAULT_PASSWORD);
    }

    @Test
    void shouldUpdateServiceSpecificValues() {
        ActiveMqDevServicesProperties properties = createProperties();

        properties.setManagementConsolePort(RoseActiveMqContainer.WEB_CONSOLE_PORT);
        properties.setUsername("myusername");
        properties.setPassword("mypassword");

        assertThat(properties.getManagementConsolePort()).isEqualTo(RoseActiveMqContainer.WEB_CONSOLE_PORT);
        assertThat(properties.getUsername()).isEqualTo("myusername");
        assertThat(properties.getPassword()).isEqualTo("mypassword");
    }

}
