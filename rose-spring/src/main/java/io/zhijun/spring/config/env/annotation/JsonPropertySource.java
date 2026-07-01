package io.zhijun.spring.config.env.annotation;

import io.zhijun.spring.config.env.support.JsonPropertySourceFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link PropertySource} variant for JSON resources.
 *
 * @since 0.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@PropertySource(value = {}, factory = JsonPropertySourceFactory.class)
public @interface JsonPropertySource {

    @AliasFor(annotation = PropertySource.class)
    String name() default "";

    @AliasFor(annotation = PropertySource.class)
    String[] value() default {};

    @AliasFor(annotation = PropertySource.class)
    boolean ignoreResourceNotFound() default false;

    @AliasFor(annotation = PropertySource.class)
    String encoding() default "";
}
