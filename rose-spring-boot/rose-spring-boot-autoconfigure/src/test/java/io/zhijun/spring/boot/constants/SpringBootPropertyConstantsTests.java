package io.zhijun.spring.boot.constants;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SpringBootPropertyConstantsTests {

    @Test
    void shouldExposeSpringAutoConfigureExcludeProperty() {
        assertThat(SpringBootPropertyConstants.SPRING_AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME)
                .isEqualTo("spring.autoconfigure.exclude");
    }

    @Test
    void shouldExposeAttachedPropertySourceName() {
        assertThat(SpringBootPropertyConstants.ATTACHED_PROPERTY_SOURCE_NAME)
                .isEqualTo("configurationProperties");
    }
}
