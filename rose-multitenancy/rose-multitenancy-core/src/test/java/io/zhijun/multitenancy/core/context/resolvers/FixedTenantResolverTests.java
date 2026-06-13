package io.zhijun.multitenancy.core.context.resolvers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link FixedTenantResolver}.
 */
class FixedTenantResolverTests {

    @Test
    void whenNullCustomValueThenThrow() {
        assertThatThrownBy(() -> new FixedTenantResolver(null)).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tenantIdentifier cannot be null or empty");
    }

    @Test
    void whenEmptyCustomValueThenThrow() {
        assertThatThrownBy(() -> new FixedTenantResolver("")).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tenantIdentifier cannot be null or empty");
    }

    @Test
    void whenDefaultIsUsedAsFixedTenant() {
        String expectedTenantIdentifier = "default";
        FixedTenantResolver fixedTenantResolver = new FixedTenantResolver();
        String actualTenantIdentifier = fixedTenantResolver.resolveTenantIdentifier(this);
        assertThat(actualTenantIdentifier).isEqualTo(expectedTenantIdentifier);
    }

    @Test
    void whenCustomValueIsUsedAsFixedTenant() {
        String expectedTenantIdentifier = "beans";
        FixedTenantResolver fixedTenantResolver = new FixedTenantResolver(expectedTenantIdentifier);
        String actualTenantIdentifier = fixedTenantResolver.resolveTenantIdentifier(this);
        assertThat(actualTenantIdentifier).isEqualTo(expectedTenantIdentifier);
    }

}
