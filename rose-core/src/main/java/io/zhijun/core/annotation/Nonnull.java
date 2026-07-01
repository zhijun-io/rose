package io.zhijun.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@org.jspecify.annotations.NonNull
@Documented
@Target(TYPE_USE)
@Retention(RUNTIME)
public @interface Nonnull {
}
