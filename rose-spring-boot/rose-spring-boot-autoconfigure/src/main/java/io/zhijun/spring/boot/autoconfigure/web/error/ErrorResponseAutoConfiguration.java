package io.zhijun.spring.boot.autoconfigure.web.error;

import io.zhijun.spring.core.context.ApplicationExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Rose error response handling in Spring MVC applications.
 */
@AutoConfiguration
@ConditionalOnClass(
        name = {
            "org.springframework.web.bind.annotation.ControllerAdvice",
            "org.springframework.web.bind.annotation.ExceptionHandler",
            "org.springframework.http.ResponseEntity"
        })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ErrorResponseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApplicationExceptionHandler applicationExceptionHandler() {
        return new ApplicationExceptionHandler();
    }
}
