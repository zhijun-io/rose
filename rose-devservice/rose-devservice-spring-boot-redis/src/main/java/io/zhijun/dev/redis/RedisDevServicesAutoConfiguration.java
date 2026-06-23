package io.zhijun.dev.redis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.api.provider.DevServiceCategories;
import io.zhijun.dev.api.provider.DevServiceProvider;
import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.dev.core.registration.DevServiceRegistrar;
import io.zhijun.dev.core.registration.DevServiceRegistry;
import io.zhijun.dev.redis.RedisDevServicesAutoConfiguration.RedisDevServiceRegistrar;

/**
 * Redis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("redis")
@EnableConfigurationProperties(RedisDevServiceProperties.class)
@Import(RedisDevServiceRegistrar.class)
public final class RedisDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider redisDevServiceProvider() {
        return DevServiceProvider.of("redis", DevServiceCategories.REDIS);
    }

    static class RedisDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            final RedisDevServiceProperties properties = bindProperties(
                    RedisDevServiceProperties.CONFIG_PREFIX, RedisDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServiceRegistry.ServiceSpec service) {
                    service
                            .name("redis")
                            .description("Redis Dev Service")
                            .container(new java.util.function.Consumer<DevServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServiceRegistry.ContainerSpec container) {
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
