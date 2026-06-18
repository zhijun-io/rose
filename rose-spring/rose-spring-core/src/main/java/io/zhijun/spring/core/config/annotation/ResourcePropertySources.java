package io.zhijun.spring.core.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.zhijun.spring.core.config.support.ResourcePropertySourcesLoader;

import org.springframework.context.annotation.Import;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

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
