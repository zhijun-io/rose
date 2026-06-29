package io.zhijun.multitenancy.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.boot.autoconfigure.logging.TenantLoggingProperties;

/**
 * Unit test for {@link TenantLoggingProperties}.
 */
class TenantLoggingPropertiesTests {

    @Test
    void defaultsAndMutators() {
        TenantLoggingProperties properties = new TenantLoggingProperties();

        assertThat(properties.getMdc().isEnabled()).isTrue();
        assertThat(properties.getMdc().getKeyName()).isEqualTo("tenantId");

        properties.getMdc().setEnabled(false);
        properties.getMdc().setKeyName("multitenancy.id");

        assertThat(properties.getMdc().isEnabled()).isFalse();
        assertThat(properties.getMdc().getKeyName()).isEqualTo("multitenancy.id");
    }
}
