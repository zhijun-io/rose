package io.zhijun.spring.config.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container for repeatable {@link DefaultPropertiesPropertySource}.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@Import(DefaultPropertiesPropertySourcesLoader.class)
public @interface DefaultPropertiesPropertySources {

    DefaultPropertiesPropertySource[] value();
}
