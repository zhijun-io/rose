package io.zhijun.devservice.boot.autoconfigure.redis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.boot.registration.DevServiceAutoConfigRegistrar;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;

/**
 * Redis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(RedisDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(RedisDevServiceProperties.class)
@Import(DevServiceAutoConfigRegistrar.class)
public final class RedisDevServicesAutoConfiguration {

    static final DevServiceConnectorDescriptor<RedisDevServiceProperties, RedisContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<RedisDevServiceProperties, RedisContainer>builder()
                    .propertiesType(RedisDevServiceProperties.class)
                    .configPrefix(RedisDevServiceProperties.CONFIG_PREFIX)
                    .serviceName(RedisDevServiceProperties.SERVICE_NAME)
                    .displayName("Redis Dev Service")
                    .category(DevServiceCategory.REDIS)
                    .containerClass(RedisContainer.class)
                    .containerFactory(RedisContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty(
                                "spring.redis.host",
                                () -> registrar.requireRunningContainer().getRedisHost());
                        registrar.addDynamicProperty(
                                "spring.redis.port",
                                () -> registrar.requireRunningContainer().getRedisPort());
                    })
                    .build();
}
