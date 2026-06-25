package io.zhijun.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * The annotated type is not thread-safe.
 *
 * @see ThreadSafe
 */
@Documented
@Target(TYPE)
@Retention(CLASS)
public @interface NotThreadSafe {
}
