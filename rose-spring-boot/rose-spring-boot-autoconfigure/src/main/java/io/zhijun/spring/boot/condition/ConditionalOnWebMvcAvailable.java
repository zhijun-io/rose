package io.zhijun.spring.boot.condition;

import io.zhijun.spring.web.method.support.HandlerMethodInterceptor;
import io.zhijun.spring.webmvc.annotation.EnableWebMvcExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.zhijun.spring.boot.constants.PropertyConstants.ROSE_SPRING_BOOT_WEBMVC_ENABLED_PROPERTY_NAME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * 检查 Spring Web MVC 和 Servlet Web 应用是否可用的条件注解。
 *
 * @since 1.0.0
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass(value = {
        Servlet.class,                   // Servlet API
        ApplicationContext.class,        // Spring Context API
        HandlerMethod.class,             // Spring Web API
        DispatcherServlet.class,         // Spring Web MVC API
        HandlerMethodInterceptor.class,  // Rose Spring Web API
        EnableWebMvcExtension.class      // Rose Spring Web MVC API
})
@ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEBMVC_ENABLED_PROPERTY_NAME, matchIfMissing = true)
public @interface ConditionalOnWebMvcAvailable {
}
