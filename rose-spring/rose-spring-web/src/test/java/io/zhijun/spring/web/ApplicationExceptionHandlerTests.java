package io.zhijun.spring.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.zhijun.core.exception.ApplicationException;
import io.zhijun.core.exception.ErrorCodes;
import io.zhijun.core.exception.ErrorResponse;

class ApplicationExceptionHandlerTests {

    private final ApplicationExceptionHandler handler = new ApplicationExceptionHandler();

    @Test
    void mapsNotFoundTo404() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.NOT_FOUND);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void mapsConflictTo409() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.CONFLICT);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void mapsValidationErrorTo400() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.VALIDATION_ERROR);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void mapsBadRequestTo400() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.BAD_REQUEST);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void mapsInternalErrorTo500() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.INTERNAL_ERROR);
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void defaultsUnknownErrorCodeTo400() {
        ApplicationException ex = new ApplicationException("UNKNOWN_ERROR_CODE");
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void responseBodyMatchesException() {
        ApplicationException ex = new ApplicationException(ErrorCodes.Common.NOT_FOUND, "user not found");
        ResponseEntity<ErrorResponse> response = handler.handle(ex);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCodes.Common.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("user not found");
    }
}
