package io.zhijun.devservice.boot.autoconfigure.redis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * Redis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("redis")
@EnableConfigurationProperties(RedisDevServiceProperties.class)
@Import(RedisDevServicesAutoConfiguration.RedisDevServiceRegistrar.class)
public final class RedisDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<RedisDevServiceProperties, RoseRedisContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<RedisDevServiceProperties, RoseRedisContainer>builder()
                    .propertiesType(RedisDevServiceProperties.class)
                    .configPrefix(RedisDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("redis")
                    .displayName("Redis Dev Service")
                    .category(DevServiceCategories.REDIS)
                    .containerClass(RoseRedisContainer.class)
                    .containerFactory(RoseRedisContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty("spring.redis.host",
                                () -> registrar.requireRunningContainer().getRedisHost());
                        registrar.addDynamicProperty("spring.redis.port",
                                () -> registrar.requireRunningContainer().getRedisPort());
                    })
                    .build();

    static final class RedisDevServiceRegistrar
            extends ContainerDevServiceRegistrar<RedisDevServiceProperties, RoseRedisContainer> {

        RedisDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
