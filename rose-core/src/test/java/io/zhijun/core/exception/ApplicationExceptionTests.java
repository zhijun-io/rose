package io.zhijun.core.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ApplicationExceptionTests {

    @Test
    void storesStructuredFields() {
        ApplicationException exception = new ApplicationException("USER_NOT_FOUND", "user missing")
                .withDetail("userId", 1L)
                .retryable();

        assertThat(exception.getErrorCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("user missing");
        assertThat(exception.isRetryable()).isTrue();
        assertThat(exception.getDetail("userId", Long.class)).isEqualTo(1L);
    }

    @Test
    void keepsDetailsImmutable() {
        ApplicationException exception = new ApplicationException("E1").withDetail("k", "v");

        assertThatThrownBy(() -> exception.getDetails().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsBlankErrorCode() {
        assertThatThrownBy(() -> new ApplicationException(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("errorCode");
    }

    @Test
    void rejectsInvalidErrorCodeFormat() {
        assertThatThrownBy(() -> new ApplicationException("user.not-found"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ErrorCodes.PATTERN);
    }

    @Test
    void rejectsTypeMismatchWhenReadingDetail() {
        ApplicationException exception = new ApplicationException("E1").withDetail("count", 1);

        assertThatThrownBy(() -> exception.getDetail("count", String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("count");
    }
}
