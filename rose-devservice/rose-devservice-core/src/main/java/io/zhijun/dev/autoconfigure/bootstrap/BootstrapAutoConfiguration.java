package io.zhijun.dev.autoconfigure.bootstrap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.dev.autoconfigure.bootstrap.dev.BootstrapDevConfiguration;
import io.zhijun.dev.autoconfigure.bootstrap.test.BootstrapTestConfiguration;

/**
 * Auto-configuration for Dev Services bootstrap.
 */
@Configuration(proxyBeanMethods = false)
@Import({
        BootstrapDevConfiguration.class,
        BootstrapTestConfiguration.class
})
@EnableConfigurationProperties(BootstrapProperties.class)
public final class BootstrapAutoConfiguration {
}
