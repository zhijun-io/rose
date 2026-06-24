package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
import io.zhijun.devservice.boot.autoconfigure.rabbitmq.RabbitMqDevServicesAutoConfiguration.RabbitMqDevServiceRegistrar;

/**
 * RabbitMQ dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RabbitAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("rabbitmq")
@EnableConfigurationProperties(RabbitMqDevServiceProperties.class)
@Import(RabbitMqDevServiceRegistrar.class)
public final class RabbitMqDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider rabbitMqDevServiceProvider() {
        return DevServiceProvider.of("rabbitmq", DevServiceCategories.RABBITMQ);
    }

    static class RabbitMqDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            RabbitMqDevServiceProperties properties = bindProperties(
                    RabbitMqDevServiceProperties.CONFIG_PREFIX, RabbitMqDevServiceProperties.class);

            registry.registerDevService("rabbitmq", "RabbitMQ Dev Service",
                    RoseRabbitMqContainer.class, () -> new RoseRabbitMqContainer(properties));

            addDynamicProperty("spring.rabbitmq.host", () -> rabbitContainer().getHost());
            addDynamicProperty("spring.rabbitmq.port", () -> rabbitContainer().getAmqpPort());
        }

        private RoseRabbitMqContainer rabbitContainer() {
            RoseRabbitMqContainer container = getBeanFactory().getBean(RoseRabbitMqContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
