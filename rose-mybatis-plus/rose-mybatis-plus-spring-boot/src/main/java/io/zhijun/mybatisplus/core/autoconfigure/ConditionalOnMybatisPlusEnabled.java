package io.zhijun.mybatisplus.core.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * {@link ConditionalOnProperty @ConditionalOnProperty} variant for Rose MyBatis-Plus:
 * {@code rose.mybatis-plus.enabled}.
 * <p>
 * Defaults to {@code true} when the property is absent.
 *
 * @see ConditionalOnProperty
 * @since 0.0.0.2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(prefix = "rose.mybatis-plus", name = "enabled", matchIfMissing = true)
public @interface ConditionalOnMybatisPlusEnabled {
}
