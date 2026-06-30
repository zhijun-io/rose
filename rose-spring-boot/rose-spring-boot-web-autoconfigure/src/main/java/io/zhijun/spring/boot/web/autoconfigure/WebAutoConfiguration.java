package io.zhijun.spring.boot.web.autoconfigure;

import io.zhijun.spring.boot.web.autoconfigure.condition.ConditionalOnWebAvailable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Rose Spring Boot Web 自动配置基础
 */
@ConditionalOnWebAvailable
@AutoConfiguration
public class WebAutoConfiguration {
}
