package io.zhijun.devservice.boot.autoconfigure.activemq;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * ActiveMQ Classic dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(ActiveMQAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("activemq")
@EnableConfigurationProperties(ActiveMqDevServiceProperties.class)
@Import(ActiveMqDevServicesAutoConfiguration.ActiveMqDevServiceRegistrar.class)
public final class ActiveMqDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<ActiveMqDevServiceProperties, ActiveMqContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<ActiveMqDevServiceProperties, ActiveMqContainer>builder()
                    .propertiesType(ActiveMqDevServiceProperties.class)
                    .configPrefix(ActiveMqDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("activemq")
                    .displayName("ActiveMQ Dev Service")
                    .category(DevServiceCategories.JMS)
                    .containerClass(ActiveMqContainer.class)
                    .containerFactory(ActiveMqContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty("spring.activemq.broker-url",
                                () -> registrar.requireRunningContainer().getBrokerUrl());
                        registrar.addDynamicProperty("spring.activemq.user",
                                () -> registrar.requireRunningContainer().getUsername());
                        registrar.addDynamicProperty("spring.activemq.password",
                                () -> registrar.requireRunningContainer().getPassword());
                    })
                    .build();

    static final class ActiveMqDevServiceRegistrar
            extends ContainerDevServiceRegistrar<ActiveMqDevServiceProperties, ActiveMqContainer> {

        ActiveMqDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
