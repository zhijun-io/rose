package io.zhijun.spring.core.web.error;

import io.zhijun.core.exception.ApplicationException;
import io.zhijun.core.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Spring MVC adapter that maps {@link ApplicationException} to {@link ErrorResponse}.
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handle(ApplicationException exception) {
        return ResponseEntity.status(status(exception)).body(exception.toErrorResponse());
    }

    protected HttpStatus status(ApplicationException exception) {
        return HttpStatus.BAD_REQUEST;
    }
}
