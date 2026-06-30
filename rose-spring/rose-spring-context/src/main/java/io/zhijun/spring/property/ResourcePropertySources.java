package io.zhijun.spring.propertysource;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import io.zhijun.spring.propertysource.ResourcePropertySourcesLoader;

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
