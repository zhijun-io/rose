package io.zhijun.devservice.boot.autoconfigure.bootstrap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Development mode bootstrap configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BootstrapDevProperties.class)
public final class BootstrapDevConfiguration {}
