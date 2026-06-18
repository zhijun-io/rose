package io.zhijun.boot.constants;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringBootPropertyConstantsTest {

    @Test
    void shouldExposeSpringBootPropertyNames() {
        assertThat(SpringBootPropertyConstants.SPRING_AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME)
                .isEqualTo("spring.autoconfigure.exclude");
        assertThat(SpringBootPropertyConstants.ATTACHED_PROPERTY_SOURCE_NAME)
                .isEqualTo("configurationProperties");
    }
}
