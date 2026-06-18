package io.zhijun.dev.services.rabbitmq;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.rabbitmq.RabbitMqDevServicesAutoConfiguration.RabbitMqDevServicesRegistrar;

/**
 * RabbitMQ dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RabbitAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("rabbitmq")
@EnableConfigurationProperties(RabbitMqDevServicesProperties.class)
@Import(RabbitMqDevServicesRegistrar.class)
public final class RabbitMqDevServicesAutoConfiguration {

    static class RabbitMqDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final RabbitMqDevServicesProperties properties = bindProperties(
                    RabbitMqDevServicesProperties.CONFIG_PREFIX, RabbitMqDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("rabbitmq")
                            .description("RabbitMQ Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
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
