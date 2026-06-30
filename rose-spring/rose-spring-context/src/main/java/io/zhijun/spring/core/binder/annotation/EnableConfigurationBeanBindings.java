package io.zhijun.spring.core.binder.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Container for repeatable {@link EnableConfigurationBeanBinding} annotation.
 * <p>
 * Required by Java {@link java.lang.annotation.Repeatable}; also allows explicit grouping:
 *
 * <pre>
 * &#64;EnableConfigurationBeanBindings({
 *     &#64;EnableConfigurationBeanBinding(prefix = "usr", type = User.class),
 *     &#64;EnableConfigurationBeanBinding(prefix = "app", type = App.class)
 * })
 * </pre>
 *
 * @see ConfigurationBeanBindingsRegistrar
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(ConfigurationBeanBindingsRegistrar.class)
public @interface EnableConfigurationBeanBindings {

    EnableConfigurationBeanBinding[] value();
}
