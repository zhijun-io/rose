package io.zhijun.spring.boot.autoconfigure.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 检查指定属性前缀是否存在于 Environment 中的条件注解。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnPropertyPrefixCondition.class)
public @interface ConditionalOnPropertyPrefix {

    /**
     * 属性前缀值列表。
     */
    String[] value();
}
