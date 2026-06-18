package io.zhijun.spring.core.propertysource.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Comparator;

import io.zhijun.spring.core.propertysource.support.DefaultResourceComparator;
import io.zhijun.spring.core.propertysource.support.JsonPropertySourceFactory;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.Resource;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * JSON property source annotation.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@ResourcePropertySource(factory = JsonPropertySourceFactory.class)
public @interface JsonPropertySource {

    @AliasFor(annotation = ResourcePropertySource.class)
    String name() default "";

    @AliasFor(annotation = ResourcePropertySource.class)
    boolean autoRefreshed() default false;

    @AliasFor(annotation = ResourcePropertySource.class)
    boolean first() default false;

    @AliasFor(annotation = ResourcePropertySource.class)
    String before() default "";

    @AliasFor(annotation = ResourcePropertySource.class)
    String after() default "";

    @AliasFor(annotation = ResourcePropertySource.class)
    String[] value() default {};

    @AliasFor(annotation = ResourcePropertySource.class)
    Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;

    @AliasFor(annotation = ResourcePropertySource.class)
    boolean ignoreResourceNotFound() default false;

    @AliasFor(annotation = ResourcePropertySource.class)
    String encoding() default "UTF-8";
}
