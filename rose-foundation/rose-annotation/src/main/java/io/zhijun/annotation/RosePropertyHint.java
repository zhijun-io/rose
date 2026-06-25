package io.zhijun.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents a {@code rose.*} configuration property for IDE metadata
 * ({@code META-INF/additional-spring-configuration-metadata.json}).
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(RosePropertyHints.class)
@Documented
public @interface RosePropertyHint {

    /**
     * Full property name, e.g. {@code rose.dev.postgresql.enabled}.
     */
    String name();

    /**
     * Property type for tooling, e.g. {@code java.lang.Boolean}.
     */
    String type() default "java.lang.String";

    String description() default "";

    String defaultValue() default "";
}
