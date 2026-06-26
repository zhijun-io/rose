package io.zhijun.devservice.core.api.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link MessagingDevServiceProperties}.
 */
class MessagingDevServicePropertiesTests {

    @Test
    void defaults() {
        MessagingDevServiceProperties properties = new MessagingDevServiceProperties() {
        };

        assertThat(properties.getManagementConsolePort()).isEqualTo(BaseDevServiceProperties.RANDOM_PORT);
        assertThat(properties.getUsername()).isEqualTo(DevServiceCredentials.DEFAULT_USERNAME);
        assertThat(properties.getPassword()).isEqualTo(DevServiceCredentials.DEFAULT_PASSWORD);
    }

}
