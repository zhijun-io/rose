package io.zhijun.spring.config.property;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Container for repeatable resource property sources.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@Import(ResourcePropertySourcesLoader.class)
public @interface ResourcePropertySources {

    ResourcePropertySource[] value();
}
