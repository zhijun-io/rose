package io.zhijun.observation.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ObservationProperties}.
 */
class ObservationPropertiesTests {

    @Test
    void configPrefix() {
        assertThat(ObservationProperties.CONFIG_PREFIX).isEqualTo("rose.observations");
    }

}
