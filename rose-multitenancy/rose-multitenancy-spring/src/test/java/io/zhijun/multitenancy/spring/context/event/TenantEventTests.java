package io.zhijun.multitenancy.spring.context.event;

import io.zhijun.multitenancy.spring.event.TenantEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantEventTests {

    @Test
    void whenNullTenantIdentifierThenThrow() {
        assertThatThrownBy(() -> new TestTenantEvent(null, this))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantIdentifier cannot be null or empty");
    }

    @Test
    void whenEmptyTenantIdentifierThenThrow() {
        assertThatThrownBy(() -> new TestTenantEvent("", this))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantIdentifier cannot be null or empty");
    }

    @Test
    void whenNullSourceThenThrow() {
        assertThatThrownBy(() -> new TestTenantEvent("multitenancy", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static class TestTenantEvent extends TenantEvent {

        TestTenantEvent(String tenantIdentifier, Object source) {
            super(tenantIdentifier, source);
        }
    }
}
