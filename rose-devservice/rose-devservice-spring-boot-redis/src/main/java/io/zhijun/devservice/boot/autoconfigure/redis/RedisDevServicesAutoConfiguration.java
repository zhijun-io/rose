package io.zhijun.devservice.boot.autoconfigure.redis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
import io.zhijun.devservice.boot.autoconfigure.redis.RedisDevServicesAutoConfiguration.RedisDevServiceRegistrar;

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
            RedisDevServiceProperties properties = bindProperties(
                    RedisDevServiceProperties.CONFIG_PREFIX, RedisDevServiceProperties.class);

            registry.registerDevService("redis", "Redis Dev Service",
                    RoseRedisContainer.class, () -> new RoseRedisContainer(properties));

            addDynamicProperty("spring.redis.host", () -> redisContainer().getRedisHost());
            addDynamicProperty("spring.redis.port", () -> redisContainer().getRedisPort());
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
