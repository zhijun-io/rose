package io.zhijun.spring.config.context.annotation;

import io.zhijun.spring.config.env.support.DefaultResourceComparator;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.PropertySourceFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Comparator;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Extended variant of Spring {@code @PropertySource}.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@PropertySourceExtension
@Repeatable(ResourcePropertySources.class)
@Import(ResourcePropertySourceLoader.class)
public @interface ResourcePropertySource {

    @AliasFor(annotation = PropertySourceExtension.class)
    String name() default "";

    @AliasFor(annotation = PropertySourceExtension.class)
    boolean autoRefreshed() default false;

    @AliasFor(annotation = PropertySourceExtension.class)
    boolean first() default false;

    @AliasFor(annotation = PropertySourceExtension.class)
    String before() default "";

    @AliasFor(annotation = PropertySourceExtension.class)
    String after() default "";

    @AliasFor(annotation = PropertySourceExtension.class)
    String[] value() default {};

    @AliasFor(annotation = PropertySourceExtension.class)
    Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;

    @AliasFor(annotation = PropertySourceExtension.class)
    boolean ignoreResourceNotFound() default false;

    @AliasFor(annotation = PropertySourceExtension.class)
    String encoding() default "${file.encoding:UTF-8}";

    @AliasFor(annotation = PropertySourceExtension.class)
    Class<? extends PropertySourceFactory> factory() default DefaultPropertySourceFactory.class;
}
