package io.zhijun.multitenancy.spring.web.annotation;

import java.lang.annotation.*;

/**
 * Annotation that is used to resolve the current multitenancy identifier as a method argument.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantId {}
