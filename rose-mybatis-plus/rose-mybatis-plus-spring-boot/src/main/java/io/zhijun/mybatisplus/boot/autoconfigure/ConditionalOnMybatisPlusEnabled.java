package io.zhijun.mybatisplus.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * {@link ConditionalOnProperty @ConditionalOnProperty} variant for Rose MyBatis-Plus:
 * {@code rose.mybatis-plus.enabled}.
 * <p>
 * Defaults to {@code true} when the property is absent.
 *
 * @see ConditionalOnProperty
 * @since 0.0.1
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(prefix = MybatisPlusProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public @interface ConditionalOnMybatisPlusEnabled {}
