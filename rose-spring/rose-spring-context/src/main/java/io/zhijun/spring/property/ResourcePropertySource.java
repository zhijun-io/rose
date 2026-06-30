package io.zhijun.spring.property;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Comparator;

import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Enhanced property source annotation.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@Repeatable(ResourcePropertySources.class)
@Import(ResourcePropertySourceLoader.class)
public @interface ResourcePropertySource {

    String name() default "";

    boolean autoRefreshed() default false;

    boolean first() default false;

    String before() default "";

    String after() default "";

    String[] value() default {};

    Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;

    boolean ignoreResourceNotFound() default false;

    String encoding() default "${file.encoding:UTF-8}";

    Class<? extends PropertySourceFactory> factory() default DefaultPropertySourceFactory.class;
}
