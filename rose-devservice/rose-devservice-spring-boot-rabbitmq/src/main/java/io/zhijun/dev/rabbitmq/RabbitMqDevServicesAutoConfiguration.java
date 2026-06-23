package io.zhijun.dev.rabbitmq;

import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.registration.LocalServiceRegistrar;
import io.zhijun.dev.core.registration.LocalServiceRegistry;
import io.zhijun.dev.rabbitmq.RabbitMqDevServicesAutoConfiguration.RabbitMqLocalServiceRegistrar;

/**
 * RabbitMQ dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RabbitAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("rabbitmq")
@EnableConfigurationProperties(RabbitMqDevServiceProperties.class)
@Import(RabbitMqLocalServiceRegistrar.class)
public final class RabbitMqDevServicesAutoConfiguration {

    static class RabbitMqLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final RabbitMqDevServiceProperties properties = bindProperties(
                    RabbitMqDevServiceProperties.CONFIG_PREFIX, RabbitMqDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<LocalServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(LocalServiceRegistry.ServiceSpec service) {
                    service
                            .name("rabbitmq")
                            .description("RabbitMQ Dev Service")
                            .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(LocalServiceRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseRabbitMqContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseRabbitMqContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty("spring.rabbitmq.host", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return rabbitContainer().getHost();
                }
            });
            addDynamicProperty("spring.rabbitmq.port", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return rabbitContainer().getAmqpPort();
                }
            });
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
