package io.zhijun.multitenancy.spring.observation;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import io.zhijun.multitenancy.spring.event.TenantContextAttachedEvent;
import io.zhijun.multitenancy.spring.event.TenantContextClosedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MdcTenantEventListenerTests {

    @Test
    void whenNullCustomValueThenThrow() {
        assertThatThrownBy(() -> new MdcTenantEventListener(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantIdentifierKey cannot be null or empty");
    }

    @Test
    void whenEmptyCustomValueThenThrow() {
        assertThatThrownBy(() -> new MdcTenantEventListener("")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantIdentifierKey cannot be null or empty");
    }

    @Test
    void whenDefaultValueIsUsedAsKey() {
        String tenantKey = "tenantId";
        String tenantValue = "acme";
        MdcTenantEventListener listener = new MdcTenantEventListener();

        listener.onAttached(new TenantContextAttachedEvent(tenantValue, this));

        assertThat(MDC.get(tenantKey)).isEqualTo(tenantValue);

        listener.onClosed(new TenantContextClosedEvent(tenantValue, this));

        assertThat(MDC.get(tenantKey)).isNull();
    }

    @Test
    void whenCustomValueIsUsedAsKey() {
        String tenantKey = "tenant_id";
        String tenantValue = "acme";
        MdcTenantEventListener listener = new MdcTenantEventListener(tenantKey);

        listener.onAttached(new TenantContextAttachedEvent(tenantValue, this));

        assertThat(MDC.get(tenantKey)).isEqualTo(tenantValue);

        listener.onClosed(new TenantContextClosedEvent(tenantValue, this));

        assertThat(MDC.get(tenantKey)).isNull();
    }

}
