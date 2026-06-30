package io.zhijun.devservice.core.api.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DevServiceCategory}.
 */
class DevServiceCategoryTests {

    @Test
    void idsAreStable() {
        assertThat(DevServiceCategory.JDBC.id()).isEqualTo("jdbc");
        assertThat(DevServiceCategory.OLLAMA.id()).isEqualTo("ollama");
    }
}
