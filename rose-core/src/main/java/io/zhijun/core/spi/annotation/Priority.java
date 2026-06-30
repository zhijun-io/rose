package io.zhijun.core.spi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Priority for SPI implementations. Lower values have higher priority.
 * <p>Replaces {@code javax.annotation.Priority} which is not part of the JDK.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Priority {

    /**
     * Priority value. Lower values indicate higher priority.
     */
    int value() default Integer.MAX_VALUE;
}
