package io.zhijun.core.annotation;

import java.lang.annotation.*;

@org.jspecify.annotations.Nullable
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface Nullable {
}
