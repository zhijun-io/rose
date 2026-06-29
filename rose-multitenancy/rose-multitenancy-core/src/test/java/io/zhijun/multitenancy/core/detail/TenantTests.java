package io.zhijun.multitenancy.core.detail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Tenant}.
 */
class TenantTests {

    @Test
    void whenIdentifierIsNullThenThrow() {
        assertThatThrownBy(() -> Tenant.builder().build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identifier cannot be null or empty");
    }

    @Test
    void whenIdentifierIsEmptyThenThrow() {
        assertThatThrownBy(() -> Tenant.builder().identifier("").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identifier cannot be null or empty");
    }

    @Test
    void whenAttributesIsNullThenThrow() {
        assertThatThrownBy(() ->
                        Tenant.builder().identifier("acme").attributes(null).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("attributes cannot be null");
    }
}
