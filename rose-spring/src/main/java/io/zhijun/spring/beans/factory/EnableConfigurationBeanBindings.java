package io.zhijun.spring.beans.factory;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A container annotation that holds multiple {@link EnableConfigurationBeanBinding} annotations.
 * It is used to apply several configuration bean bindings at once.
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Basic Usage</h4>
 * <pre>{@code
 * @EnableConfigurationBeanBindings(value = {
 *     @EnableConfigurationBeanBinding(name = "myBean1", value = MyBean1.class),
 *     @EnableConfigurationBeanBinding(name = "myBean2", value = MyBean2.class)
 * })
 * public class MyConfiguration {
 * }
 * }</pre>
 *
 * <h4>Java 8+ {@link Repeatable @Repeatable} Usage</h4>
 * <pre>{@code
 * @EnableConfigurationBeanBinding(name = "myBean1", value = MyBean1.class)
 * @EnableConfigurationBeanBinding(name = "myBean2", value = MyBean2.class)
 * public class MyConfiguration {
 * }
 * }</pre
 *
 * <p>The above example will register two configuration beans with names "myBean1" and "myBean2"
 * bound to their respective classes.</p>
 *
 * @since 1.0.0
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(ConfigurationBeanBindingsRegister.class)
public @interface EnableConfigurationBeanBindings {

    /**
     * @return the array of {@link EnableConfigurationBeanBinding EnableConfigurationBeanBindings}
     */
    EnableConfigurationBeanBinding[] value();
}
