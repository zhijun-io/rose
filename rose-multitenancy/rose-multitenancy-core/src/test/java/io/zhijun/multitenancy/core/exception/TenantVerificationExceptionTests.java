package io.zhijun.multitenancy.core.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
