package io.zhijun.devservice.boot.autoconfigure;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Enables a dev service module when globally and locally enabled.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnDevServiceEnabledCondition.class)
public @interface ConditionalOnDevServiceEnabled {

    String value();
}
