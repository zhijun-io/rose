package io.zhijun.devservice.core.api.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DevServiceProvider}.
 */
class DevServiceProviderTests {

    @Test
    void of() {
        DevServiceProvider provider = DevServiceProvider.of("postgresql", DevServiceCategory.JDBC);

        assertThat(provider.name()).isEqualTo("postgresql");
        assertThat(provider.category()).isEqualTo(DevServiceCategory.JDBC);
    }
}
