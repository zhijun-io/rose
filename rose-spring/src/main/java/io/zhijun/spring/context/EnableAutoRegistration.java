package io.zhijun.spring.context;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 启用 {@link AutoRegistrationBean} SPI 自动注册。
 * <p>
 * 标注在任意 {@code @Configuration} 类上即可：从 {@code META-INF/services/} 发现
 * 所有 {@link AutoRegistrationBean} 实现，按照优先级排序后注册为 Spring Bean。
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Import(AutoRegistrationRegistrar.class)
public @interface EnableAutoRegistration {
}
