package io.zhijun.spring.binder;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Declares that properties under a given {@link #prefix()} should be bound into one or more Spring beans
 * of {@link #type()}.
 * <p>
 * This is the Rose alternative to Boot {@code @ConfigurationProperties}: it works with plain
 * {@code spring-context}, registers beans via {@link Import}, and supports env hot-reload through
 * {@link io.zhijun.spring.binder.ConfigurationBeanBindingRefreshable}.
 * <p>
 * <b>Startup flow</b>
 * <ol>
 * <li>{@link ConfigurationBeanBindingRegistrar} reads {@code prefix.*} from the {@code Environment}
 * and registers bean definition(s).</li>
 * <li>{@link ConfigurationBeanBindingPostProcessor} binds the flat property map onto each bean
 * before initialization.</li>
 * </ol>
 * <p>
 * <b>Example (single bean)</b>
 *
 * <pre>
 * &#64;EnableConfigurationBeanBinding(prefix = "usr", type = User.class)
 * &#64;Configuration
 * class AppConfig { }
 *
 * // usr.id=m, usr.name=rose  →  Bean "m" of type User with name=rose
 * </pre>
 * <p>
 * <b>Example ({@link #multiple()} = true)</b>
 *
 * <pre>
 * // users.u1.name=a, users.u2.name=b  →  two User beans named "u1" and "u2"
 * </pre>
 *
 * @see EnableConfigurationBeanBindings
 * @see ConfigurationBeanBindingRegistrar
 * @see ConfigurationBeanBindingPostProcessor
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

    /**
     * Property prefix in the {@code Environment} (required). Placeholders such as {@code ${app.id}} are resolved.
     * Only keys equal to {@code prefix} or starting with {@code prefix.} are considered.
     */
    String prefix();

    /**
     * Type of the configuration bean to register and bind (required).
     */
    Class<?> type();

    /**
     * When {@code false} (default), all keys under {@link #prefix()} bind to a single bean.
     * When {@code true}, the first segment after the prefix defines the bean name
     * (e.g. {@code users.u1.name} → bean {@code u1}).
     */
    boolean multiple() default DEFAULT_MULTIPLE;

    /**
     * Passed to {@link org.springframework.validation.DataBinder#setIgnoreUnknownFields(boolean)}.
     */
    boolean ignoreUnknownFields() default DEFAULT_IGNORE_UNKNOWN_FIELDS;

    /**
     * Passed to {@link org.springframework.validation.DataBinder#setIgnoreInvalidFields(boolean)}.
     */
    boolean ignoreInvalidFields() default DEFAULT_IGNORE_INVALID_FIELDS;
}
