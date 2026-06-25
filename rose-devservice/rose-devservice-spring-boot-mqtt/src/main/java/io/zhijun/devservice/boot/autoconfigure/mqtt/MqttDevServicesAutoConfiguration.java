package io.zhijun.devservice.boot.autoconfigure.mqtt;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.autoconfigure.mqtt.MqttDevServicesAutoConfiguration.MqttDevServiceRegistrar;

/**
 * MQTT dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("mqtt")
@EnableConfigurationProperties(MqttDevServiceProperties.class)
@Import(MqttDevServiceRegistrar.class)
public final class MqttDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider mqttDevServiceProvider() {
        return DevServiceProvider.of("mqtt", DevServiceCategories.MQTT);
    }

    static class MqttDevServiceRegistrar
            extends ContainerDevServiceRegistrar<MqttDevServiceProperties, RoseHiveMQContainer> {

        @Override
        protected Class<MqttDevServiceProperties> getPropertiesType() {
            return MqttDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return MqttDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "mqtt";
        }

        @Override
        protected String getDisplayName() {
            return "MQTT Dev Service";
        }

        @Override
        protected Class<RoseHiveMQContainer> getContainerClass() {
            return RoseHiveMQContainer.class;
        }

        @Override
        protected RoseHiveMQContainer createContainer(MqttDevServiceProperties properties) {
            return new RoseHiveMQContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("mqtt.server.uri", () -> requireRunningContainer().getBrokerUrl());
            addDynamicProperty("spring.mqtt.url", () -> requireRunningContainer().getBrokerUrl());
        }
    }
}
