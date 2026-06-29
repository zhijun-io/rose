package io.zhijun.multitenancy.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link FixedTenantResolutionProperties}.
 */
class FixedTenantResolutionPropertiesTests {

    @Test
    void defaultsAndMutators() {
        FixedTenantResolutionProperties properties = new FixedTenantResolutionProperties();

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getTenantIdentifier()).isEqualTo("default");

        properties.setEnabled(true);
        properties.setTenantIdentifier("acme");

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getTenantIdentifier()).isEqualTo("acme");
    }
}
