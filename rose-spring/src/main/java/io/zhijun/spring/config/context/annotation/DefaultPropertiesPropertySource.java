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
 * Adds entries to the {@code defaultProperties} property source.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@Repeatable(DefaultPropertiesPropertySources.class)
@Import(DefaultPropertiesPropertySourceLoader.class)
public @interface DefaultPropertiesPropertySource {

    String[] properties() default {};

    @AliasFor("locations")
    String[] value() default {};

    @AliasFor("value")
    String[] locations() default {};

    Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;

    boolean ignoreResourceNotFound() default false;

    String encoding() default "UTF-8";

    Class<? extends PropertySourceFactory> factory() default DefaultPropertySourceFactory.class;
}
