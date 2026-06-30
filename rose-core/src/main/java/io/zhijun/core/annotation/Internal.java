package io.zhijun.core.annotation;

import java.lang.annotation.*;


/**
 * Marks an API as internal to Rose; not a stable consumer contract.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE})
@Documented
public @interface Internal {
}
