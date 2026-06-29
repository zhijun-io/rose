package io.zhijun.devservice.boot.autoconfigure.rabbitmq;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;

/**
 * RabbitMQ dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(RabbitAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(RabbitMqDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(RabbitMqDevServiceProperties.class)
@Import(RabbitMqDevServicesAutoConfiguration.RabbitMqDevServiceRegistrar.class)
public final class RabbitMqDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<RabbitMqDevServiceProperties, RabbitMqContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<RabbitMqDevServiceProperties, RabbitMqContainer>builder()
                    .propertiesType(RabbitMqDevServiceProperties.class)
                    .configPrefix(RabbitMqDevServiceProperties.CONFIG_PREFIX)
                    .serviceName(RabbitMqDevServiceProperties.SERVICE_NAME)
                    .displayName("RabbitMQ Dev Service")
                    .category(DevServiceCategory.RABBITMQ)
                    .containerClass(RabbitMqContainer.class)
                    .containerFactory(RabbitMqContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty(
                                "spring.rabbitmq.host",
                                () -> registrar.requireRunningContainer().getHost());
                        registrar.addDynamicProperty(
                                "spring.rabbitmq.port",
                                () -> registrar.requireRunningContainer().getAmqpPort());
                    })
                    .build();

    static final class RabbitMqDevServiceRegistrar
            extends ContainerDevServiceRegistrar<RabbitMqDevServiceProperties, RabbitMqContainer> {

        RabbitMqDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
