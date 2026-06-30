package io.zhijun.spring.web;

import io.zhijun.core.exception.ApplicationException;
import io.zhijun.core.exception.ErrorCodes;
import io.zhijun.core.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring MVC 适配器，将 {@link ApplicationException} 映射为 HTTP 响应。
 * <p>errorCode → HTTP 状态码映射可通过子类重写 {@link #statusMappings()} 扩展。
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handle(ApplicationException exception) {
        return ResponseEntity.status(status(exception)).body(exception.toErrorResponse());
    }

    /**
     * 返回 errorCode → HttpStatus 映射表。
     * <p>子类可重写此方法扩展或替换映射规则。
     */
    protected Map<String, HttpStatus> statusMappings() {
        Map<String, HttpStatus> map = new HashMap<String, HttpStatus>();
        map.put(ErrorCodes.Common.NOT_FOUND,        HttpStatus.NOT_FOUND);
        map.put(ErrorCodes.Common.CONFLICT,         HttpStatus.CONFLICT);
        map.put(ErrorCodes.Common.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        map.put(ErrorCodes.Common.BAD_REQUEST,      HttpStatus.BAD_REQUEST);
        map.put(ErrorCodes.Common.INTERNAL_ERROR,   HttpStatus.INTERNAL_SERVER_ERROR);
        return map;
    }

    private HttpStatus status(ApplicationException exception) {
        return statusMappings().getOrDefault(exception.getErrorCode(), HttpStatus.BAD_REQUEST);
    }
}
