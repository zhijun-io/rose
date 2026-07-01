package io.zhijun.spring.config.context.annotation;

import io.zhijun.spring.config.env.support.DefaultResourceComparator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.PropertySourceFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Comparator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Meta-annotation extending Spring property source semantics.
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
public @interface PropertySourceExtension {

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
