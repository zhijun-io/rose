package io.zhijun.spring.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container for repeatable {@link EnableConfigurationBeanBinding} annotations.
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(ConfigurationBeanBindingsRegistrar.class)
public @interface EnableConfigurationBeanBindings {

    EnableConfigurationBeanBinding[] value();
}
