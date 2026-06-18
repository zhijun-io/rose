package io.zhijun.spring.core.binder.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.env.PropertySources;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enables annotation-driven configuration beans from {@link PropertySources properties}.
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(ConfigurationBeanBindingRegistrar.class)
@Repeatable(EnableConfigurationBeanBindings.class)
public @interface EnableConfigurationBeanBinding {

    boolean DEFAULT_MULTIPLE = false;

    boolean DEFAULT_IGNORE_UNKNOWN_FIELDS = true;

    boolean DEFAULT_IGNORE_INVALID_FIELDS = true;

    String prefix();

    Class<?> type();

    boolean multiple() default DEFAULT_MULTIPLE;

    boolean ignoreUnknownFields() default DEFAULT_IGNORE_UNKNOWN_FIELDS;

    boolean ignoreInvalidFields() default DEFAULT_IGNORE_INVALID_FIELDS;
}
