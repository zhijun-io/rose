package io.zhijun.devservice.boot.autoconfigure.ollama;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Whether Ollama native connection is unavailable when running the application in dev mode.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnOllamaNativeUnavailable.class)
public @interface ConditionalOnOllamaNativeUnavailable {}
