package io.zhijun.core.annotation;

import javax.annotation.meta.TypeQualifierNickname;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.annotation.meta.When.MAYBE;

/**
 * A common Microsphere annotation to declare that annotated elements can be {@code null} under
 * some circumstance.
 *
 * @see javax.annotation.Nonnull
 * @since 1.0.0
 */
@Documented
@Retention(RUNTIME)
@javax.annotation.Nonnull(when = MAYBE)
@TypeQualifierNickname
public @interface Nullable {
}
