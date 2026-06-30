package io.zhijun.multitenancy.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link TenantVerificationException}.
 */
class TenantVerificationExceptionTests {

    @Test
    void whenDefaultMessage() {
        TenantVerificationException exception = new TenantVerificationException();
        assertThat(exception).hasMessageContaining("Tenant verification failed");
        assertThat(exception.getErrorCode()).isEqualTo("TENANT_VERIFICATION_FAILED");
        assertThat(exception.isRetryable()).isFalse();
    }

    @Test
    void whenCustomMessage() {
        String message = "Custom multitenancy exception message";
        TenantVerificationException exception = new TenantVerificationException(message);
        assertThat(exception).hasMessageContaining(message);
        assertThat(exception.getErrorCode()).isEqualTo("TENANT_VERIFICATION_FAILED");
    }
}
