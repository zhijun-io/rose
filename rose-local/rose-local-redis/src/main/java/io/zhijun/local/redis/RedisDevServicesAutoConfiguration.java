package io.zhijun.local.redis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.local.api.provider.LocalServiceCategories;
import io.zhijun.local.api.provider.LocalServiceProvider;
import io.zhijun.local.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.local.core.autoconfigure.LocalServiceAutoConfiguration;
import io.zhijun.local.core.registration.LocalServiceRegistrar;
import io.zhijun.local.core.registration.LocalServiceRegistry;
import io.zhijun.local.redis.RedisDevServicesAutoConfiguration.RedisLocalServiceRegistrar;

/**
 * Redis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("redis")
@EnableConfigurationProperties(RedisLocalServiceProperties.class)
@Import(RedisLocalServiceRegistrar.class)
public final class RedisDevServicesAutoConfiguration {

    @Bean
    LocalServiceProvider redisDevServiceProvider() {
        return LocalServiceProvider.of("redis", LocalServiceCategories.REDIS);
    }

    static class RedisLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final RedisLocalServiceProperties properties = bindProperties(
                    RedisLocalServiceProperties.CONFIG_PREFIX, RedisLocalServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<LocalServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(LocalServiceRegistry.ServiceSpec service) {
                    service
                            .name("redis")
                            .description("Redis Dev Service")
                            .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(LocalServiceRegistry.ContainerSpec container) {
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
