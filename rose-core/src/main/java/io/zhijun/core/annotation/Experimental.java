package io.zhijun.core.annotation;

import java.lang.annotation.*;


/**
 * Marks an API as incubating; it may change or be removed without a major release guarantee.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface Experimental {
    String since();
}
