package io.zhijun.devservice.boot.autoconfigure.redis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
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

    static class RedisDevServiceRegistrar
            extends ContainerDevServiceRegistrar<RedisDevServiceProperties, RoseRedisContainer> {

        @Override
        protected Class<RedisDevServiceProperties> getPropertiesType() {
            return RedisDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return RedisDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "redis";
        }

        @Override
        protected String getDisplayName() {
            return "Redis Dev Service";
        }

        @Override
        protected Class<RoseRedisContainer> getContainerClass() {
            return RoseRedisContainer.class;
        }

        @Override
        protected RoseRedisContainer createContainer(RedisDevServiceProperties properties) {
            return new RoseRedisContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("spring.redis.host", () -> requireRunningContainer().getRedisHost());
            addDynamicProperty("spring.redis.port", () -> requireRunningContainer().getRedisPort());
        }
    }
}
