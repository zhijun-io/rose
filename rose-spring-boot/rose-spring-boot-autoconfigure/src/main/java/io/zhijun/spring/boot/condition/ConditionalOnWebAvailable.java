package io.zhijun.spring.boot.condition;

import io.zhijun.spring.web.method.support.HandlerMethodAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;

import javax.servlet.Servlet;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.zhijun.spring.boot.constants.PropertyConstants.ROSE_SPRING_BOOT_WEB_ENABLED_PROPERTY_NAME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * 检查 Rose Spring Web 和 Servlet Web 应用是否可用
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass({
    Servlet.class,
    HandlerMethodAdvice.class
})
@ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEB_ENABLED_PROPERTY_NAME, matchIfMissing = true)
public @interface ConditionalOnWebAvailable {
}
