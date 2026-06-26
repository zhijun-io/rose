package io.zhijun.devservice.core.api.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DevServiceCategory}.
 */
class DevServiceCategoryTests {

    @Test
    void idsAreStable() {
        assertThat(DevServiceCategory.JDBC.id()).isEqualTo("jdbc");
        assertThat(DevServiceCategory.OPENTELEMETRY.id()).isEqualTo("opentelemetry");
    }

}
