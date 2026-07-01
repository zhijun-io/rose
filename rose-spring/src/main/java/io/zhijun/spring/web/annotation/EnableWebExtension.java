package io.zhijun.spring.web.annotation;

import io.zhijun.spring.beans.BeanSource;
import io.zhijun.spring.web.metadata.WebEndpointMapping;
import io.zhijun.spring.web.util.RequestContextStrategy;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.zhijun.spring.beans.BeanSource.BEAN_FACTORY;
import static io.zhijun.spring.beans.BeanSource.JAVA_SERVICE_PROVIDER;
import static io.zhijun.spring.beans.BeanSource.SPRING_FACTORIES;
import static io.zhijun.spring.web.util.RequestContextStrategy.DEFAULT;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 启用 Spring Web 扩展功能的注解。
 *
 * @see org.springframework.web.servlet.config.annotation.EnableWebMvc
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
@Documented
@Inherited
@Import(WebExtensionBeanDefinitionRegistrar.class)
public @interface EnableWebExtension {

    /**
     * 是否注册 {@link WebEndpointMapping} 相关组件。
     */
    boolean registerWebEndpointMappings() default true;

    /**
     * 是否启用 {@link HandlerMethod} 拦截。
     */
    boolean interceptHandlerMethods() default true;

    /**
     * 是否发布 Web 扩展事件。
     */
    boolean publishEvents() default true;

    /**
     * 组件来源。
     */
    BeanSource[] sources() default {BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER};

    /**
     * {@link RequestAttributes} 存储策略。
     */
    RequestContextStrategy requestContextStrategy() default DEFAULT;
}
