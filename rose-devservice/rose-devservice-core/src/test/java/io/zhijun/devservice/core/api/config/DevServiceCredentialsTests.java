package io.zhijun.devservice.core.api.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DevServiceCredentials}.
 */
class DevServiceCredentialsTests {

    @Test
    void defaults() {
        assertThat(DevServiceCredentials.DEFAULT_USERNAME).isEqualTo("rose");
        assertThat(DevServiceCredentials.DEFAULT_PASSWORD).isEqualTo("rose");
        assertThat(DevServiceCredentials.DEFAULT_DB_NAME).isEqualTo("rose");
    }

}
