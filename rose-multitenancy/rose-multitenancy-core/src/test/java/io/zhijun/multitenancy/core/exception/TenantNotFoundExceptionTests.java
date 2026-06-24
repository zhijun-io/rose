package io.zhijun.multitenancy.core.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link TenantNotFoundException}.
 */
class TenantNotFoundExceptionTests {

    @Test
    void whenDefaultMessage() {
        TenantNotFoundException exception = new TenantNotFoundException();
        assertThat(exception).hasMessageContaining("No tenant found in the current context");
    }

    @Test
    void whenCustomMessage() {
        String message = "Custom multitenancy exception message";
        TenantNotFoundException exception = new TenantNotFoundException(message);
        assertThat(exception).hasMessageContaining(message);
    }

}
