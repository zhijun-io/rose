package io.zhijun.spring.web;

import io.zhijun.core.exception.ApplicationException;
import io.zhijun.core.exception.ErrorCodes;
import io.zhijun.core.exception.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationExceptionHandlerTests {

    private final ApplicationExceptionHandler handler = new ApplicationExceptionHandler();

    @Test
    void shouldMapNotFoundTo404() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.NOT_FOUND, "Not found", null);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldMapConflictTo409() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.CONFLICT, "Conflict", null);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldMapValidationErrorTo400() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.VALIDATION_ERROR, "Invalid", null);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldMapBadRequestTo400() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.BAD_REQUEST, "Bad request", null);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldMapInternalErrorTo500() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.INTERNAL_ERROR, "Internal error", null);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldDefaultTo400ForUnknownCodes() {
        ApplicationException ex = new ApplicationException("UNKNOWN_CODE", "Unknown", null);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldIncludeErrorResponseBody() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.NOT_FOUND, "Resource not found", null);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getBody()).isNotNull();
    }
}
