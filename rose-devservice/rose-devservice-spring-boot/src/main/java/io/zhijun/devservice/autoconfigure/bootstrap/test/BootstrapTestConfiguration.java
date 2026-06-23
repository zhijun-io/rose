package io.zhijun.devservice.autoconfigure.bootstrap.test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Test mode bootstrap configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BootstrapTestProperties.class)
public final class BootstrapTestConfiguration {
}
