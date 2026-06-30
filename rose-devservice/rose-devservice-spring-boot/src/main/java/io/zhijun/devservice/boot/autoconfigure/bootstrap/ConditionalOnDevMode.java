package io.zhijun.devservice.boot.autoconfigure.bootstrap;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Whether the application is running in dev mode.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnDevModeCondition.class)
public @interface ConditionalOnDevMode {}
