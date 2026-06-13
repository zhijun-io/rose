package io.zhijun.boot.autoconfigure.bootstrap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.boot.autoconfigure.bootstrap.dev.BootstrapDevConfiguration;
import io.zhijun.boot.autoconfigure.bootstrap.test.BootstrapTestConfiguration;

/**
 * Auto-configuration for Rose bootstrap.
 */
@Configuration(proxyBeanMethods = false)
@Import({
        BootstrapDevConfiguration.class,
        BootstrapTestConfiguration.class
})
@EnableConfigurationProperties(BootstrapProperties.class)
public final class BootstrapAutoConfiguration {
}
