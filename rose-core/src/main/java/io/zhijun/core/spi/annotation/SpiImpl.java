package io.zhijun.core.spi.annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Declares SPI implementation metadata.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpiImpl {
    /**
     * Alias of the implementation, used to get by name via {@link io.zhijun.core.spi.SpiLoader#getByName(String)}.
     * Default: class name with first letter lowercase.
     */
    String value() default "";
    /**
     * Explicit priority for the implementation. Lower values win.
     */
    int priority() default Integer.MAX_VALUE;
    /**
     * Whether this implementation is enabled.
     */
    boolean enabled() default true;
    /**
     * Whether this implementation should be treated as a singleton.
     */
    boolean singleton() default true;
    /**
     * Whether this implementation overrides lower priority implementations with the same alias.
     */
    boolean override() default false;
}
