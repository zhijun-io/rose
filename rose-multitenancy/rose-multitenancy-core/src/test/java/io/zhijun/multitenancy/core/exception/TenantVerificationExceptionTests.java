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
    }

    @Test
    void whenCustomMessage() {
        String message = "Custom multitenancy exception message";
        TenantVerificationException exception = new TenantVerificationException(message);
        assertThat(exception).hasMessageContaining(message);
    }
}
