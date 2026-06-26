package io.zhijun.devservice.boot.autoconfigure.mqtt;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * MQTT dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("mqtt")
@EnableConfigurationProperties(MqttDevServiceProperties.class)
@Import(MqttDevServicesAutoConfiguration.MqttDevServiceRegistrar.class)
public final class MqttDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<MqttDevServiceProperties, HiveMqContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<MqttDevServiceProperties, HiveMqContainer>builder()
                    .propertiesType(MqttDevServiceProperties.class)
                    .configPrefix(MqttDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("mqtt")
                    .displayName("MQTT Dev Service")
                    .category(DevServiceCategories.MQTT)
                    .containerClass(HiveMqContainer.class)
                    .containerFactory(HiveMqContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty("mqtt.server.uri",
                                () -> registrar.requireRunningContainer().getBrokerUrl());
                        registrar.addDynamicProperty("spring.mqtt.url",
                                () -> registrar.requireRunningContainer().getBrokerUrl());
                    })
                    .build();

    static final class MqttDevServiceRegistrar
            extends ContainerDevServiceRegistrar<MqttDevServiceProperties, HiveMqContainer> {

        MqttDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
