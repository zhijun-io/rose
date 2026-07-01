package io.zhijun.multitenancy.core.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.core.exception.TenantNotFoundException;

class TenantContextTests {

    @BeforeEach
    @AfterEach
    void clearThreadLocal() throws Exception {
        Field field = TenantContext.class.getDeclaredField("TENANT_IDENTIFIER");
        field.setAccessible(true);
        ThreadLocal<?> threadLocal = (ThreadLocal<?>) field.get(null);
        threadLocal.remove();
    }

    @Test
    void bindSetsTenantId() {
        assertThat(TenantContext.getTenantId()).isNull();
        try (TenantContext.Scope scope = TenantContext.bind("acme")) {
            assertThat(TenantContext.getTenantId()).isEqualTo("acme");
        }
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void bindCloseRestoresPreviousValue() {
        try (TenantContext.Scope outer = TenantContext.bind("outer")) {
            try (TenantContext.Scope inner = TenantContext.bind("inner")) {
                assertThat(TenantContext.getTenantId()).isEqualTo("inner");
            }
            assertThat(TenantContext.getTenantId()).isEqualTo("outer");
        }
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void bindRejectsNullIdentifier() {
        assertThatThrownBy(() -> TenantContext.bind(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantIdentifier");
    }

    @Test
    void bindRejectsBlankIdentifier() {
        assertThatThrownBy(() -> TenantContext.bind(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TenantContext.bind("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRequiredTenantIdThrowsWhenAbsent() {
        assertThat(TenantContext.getTenantId()).isNull();
        assertThatThrownBy(TenantContext::getRequiredTenantId)
                .isInstanceOf(TenantNotFoundException.class);
    }

    @Test
    void getRequiredTenantIdReturnsValueWhenPresent() {
        try (TenantContext.Scope scope = TenantContext.bind("acme")) {
            assertThat(TenantContext.getRequiredTenantId()).isEqualTo("acme");
        }
    }

    @Test
    void carrierRunSetsAndRestores() {
        assertThat(TenantContext.getTenantId()).isNull();
        AtomicReference<String> captured = new AtomicReference<>();
        TenantContext.where("acme").run(() -> captured.set(TenantContext.getTenantId()));
        assertThat(captured.get()).isEqualTo("acme");
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void carrierRunRestoresEvenOnException() {
        assertThat(TenantContext.getTenantId()).isNull();
        assertThatThrownBy(() -> TenantContext.where("acme").run(() -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void carrierCallReturnsValueAndRestores() throws Exception {
        assertThat(TenantContext.getTenantId()).isNull();
        String result = TenantContext.where("acme").call(() -> {
            assertThat(TenantContext.getTenantId()).isEqualTo("acme");
            return "done";
        });
        assertThat(result).isEqualTo("done");
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void whereReturnsCarrierBoundToIdentifier() {
        TenantContext.Carrier carrier = TenantContext.where("acme");
        assertThat(carrier).isNotNull();
        AtomicReference<String> captured = new AtomicReference<>();
        carrier.run(() -> captured.set(TenantContext.getTenantId()));
        assertThat(captured.get()).isEqualTo("acme");
    }
}
