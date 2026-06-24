package io.zhijun.devservice.boot.autoconfigure.mqtt;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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

    static class MqttDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            MqttDevServiceProperties properties = bindProperties(
                    MqttDevServiceProperties.CONFIG_PREFIX, MqttDevServiceProperties.class);

            registry.registerDevService("mqtt", "MQTT Dev Service",
                    RoseHiveMQContainer.class, () -> new RoseHiveMQContainer(properties));

            addDynamicProperty("mqtt.server.uri", () -> mqttContainer().getBrokerUrl());
            addDynamicProperty("spring.mqtt.url", () -> mqttContainer().getBrokerUrl());
        }

        private RoseHiveMQContainer mqttContainer() {
            RoseHiveMQContainer container = getBeanFactory().getBean(RoseHiveMQContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
