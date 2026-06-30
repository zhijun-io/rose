package io.zhijun.multitenancy.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link TenantNotFoundException}.
 */
class TenantNotFoundExceptionTests {

    @Test
    void whenDefaultMessage() {
        TenantNotFoundException exception = new TenantNotFoundException();
        assertThat(exception).hasMessageContaining("No tenant found in the current context");
        assertThat(exception.getErrorCode()).isEqualTo("TENANT_NOT_FOUND");
        assertThat(exception.isRetryable()).isFalse();
    }

    @Test
    void whenCustomMessage() {
        String message = "Custom multitenancy exception message";
        TenantNotFoundException exception = new TenantNotFoundException(message);
        assertThat(exception).hasMessageContaining(message);
        assertThat(exception.getErrorCode()).isEqualTo("TENANT_NOT_FOUND");
    }
}
