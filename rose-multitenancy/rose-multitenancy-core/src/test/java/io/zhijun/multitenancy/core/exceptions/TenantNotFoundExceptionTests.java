package io.zhijun.multitenancy.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TenantNotFoundException}.
 */
class TenantNotFoundExceptionTests {

    @Test
    void whenDefaultMessage() {
        TenantNotFoundException exception = new TenantNotFoundException();
        assertThat(exception).hasMessageContaining("No tenant found in the current context");
    }

    @Test
    void whenCustomMessage() {
        String message = "Custom tenant exception message";
        TenantNotFoundException exception = new TenantNotFoundException(message);
        assertThat(exception).hasMessageContaining(message);
    }

}
