package io.zhijun.spring.boot.autoconfigure.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Conditional;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@Documented
@ConditionalOnClass(name = "org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata")
public @interface ConditionalOnConfigurationProcessorPresent {
}
