package io.zhijun.core.annotation;

import javax.annotation.meta.TypeQualifierNickname;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A common Microsphere annotation to declare that annotated elements cannot be {@code null}.
 *
 * @see javax.annotation.Nonnull
 * @since 1.0.0
 */
@Documented
@Retention(RUNTIME)
@javax.annotation.Nonnull
@TypeQualifierNickname
public @interface Nonnull {
}
