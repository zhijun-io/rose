package io.zhijun.dev.services.redis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.services.api.provider.DevServiceCategories;
import io.zhijun.dev.services.api.provider.DevServiceProvider;
import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.redis.RedisDevServicesAutoConfiguration.RedisDevServicesRegistrar;

/**
 * Redis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("redis")
@EnableConfigurationProperties(RedisDevServicesProperties.class)
@Import(RedisDevServicesRegistrar.class)
public final class RedisDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider redisDevServiceProvider() {
        return DevServiceProvider.of("redis", DevServiceCategories.REDIS);
    }

    static class RedisDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final RedisDevServicesProperties properties = bindProperties(
                    RedisDevServicesProperties.CONFIG_PREFIX, RedisDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("redis")
                            .description("Redis Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseRedisContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseRedisContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty("spring.redis.host", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return redisContainer().getRedisHost();
                }
            });
            addDynamicProperty("spring.redis.port", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return redisContainer().getRedisPort();
                }
            });
        }

        private RoseRedisContainer redisContainer() {
            RoseRedisContainer container = getBeanFactory().getBean(RoseRedisContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
