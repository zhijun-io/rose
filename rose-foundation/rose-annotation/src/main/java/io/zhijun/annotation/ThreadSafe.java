package io.zhijun.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * The annotated type is thread-safe under its documented usage.
 *
 * @see NotThreadSafe
 */
@Documented
@Target(TYPE)
@Retention(CLASS)
public @interface ThreadSafe {
}
