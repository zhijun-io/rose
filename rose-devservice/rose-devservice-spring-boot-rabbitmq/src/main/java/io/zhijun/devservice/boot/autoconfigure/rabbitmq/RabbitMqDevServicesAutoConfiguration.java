package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
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

    static class RabbitMqDevServiceRegistrar
            extends ContainerDevServiceRegistrar<RabbitMqDevServiceProperties, RoseRabbitMqContainer> {

        @Override
        protected Class<RabbitMqDevServiceProperties> getPropertiesType() {
            return RabbitMqDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return RabbitMqDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "rabbitmq";
        }

        @Override
        protected String getDisplayName() {
            return "RabbitMQ Dev Service";
        }

        @Override
        protected Class<RoseRabbitMqContainer> getContainerClass() {
            return RoseRabbitMqContainer.class;
        }

        @Override
        protected RoseRabbitMqContainer createContainer(RabbitMqDevServiceProperties properties) {
            return new RoseRabbitMqContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("spring.rabbitmq.host", () -> requireRunningContainer().getHost());
            addDynamicProperty("spring.rabbitmq.port", () -> requireRunningContainer().getAmqpPort());
        }
    }
}
