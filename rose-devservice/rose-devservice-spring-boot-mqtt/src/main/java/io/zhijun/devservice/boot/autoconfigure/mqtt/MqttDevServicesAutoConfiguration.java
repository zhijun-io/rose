package io.zhijun.devservice.boot.autoconfigure.mqtt;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.boot.registration.DevServiceAutoConfigRegistrar;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;

/**
 * MQTT dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(MqttDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(MqttDevServiceProperties.class)
@Import(DevServiceAutoConfigRegistrar.class)
public final class MqttDevServicesAutoConfiguration {

    static final DevServiceConnectorDescriptor<MqttDevServiceProperties, HiveMqContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<MqttDevServiceProperties, HiveMqContainer>builder()
                    .propertiesType(MqttDevServiceProperties.class)
                    .configPrefix(MqttDevServiceProperties.CONFIG_PREFIX)
                    .serviceName(MqttDevServiceProperties.SERVICE_NAME)
                    .displayName("MQTT Dev Service")
                    .category(DevServiceCategory.MQTT)
                    .containerClass(HiveMqContainer.class)
                    .containerFactory(HiveMqContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty(
                                "mqtt.server.uri",
                                () -> registrar.requireRunningContainer().getBrokerUrl());
                        registrar.addDynamicProperty(
                                "spring.mqtt.url",
                                () -> registrar.requireRunningContainer().getBrokerUrl());
                    })
                    .build();
}
