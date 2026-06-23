package io.zhijun.devservice.mqtt;

import io.zhijun.devservice.core.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.api.provider.DevServiceCategories;
import io.zhijun.devservice.api.provider.DevServiceProvider;
import io.zhijun.devservice.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.core.registration.DevServiceRegistrar;
import io.zhijun.devservice.core.registration.DevServiceRegistry;
import io.zhijun.devservice.mqtt.MqttDevServicesAutoConfiguration.MqttDevServiceRegistrar;

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
            final MqttDevServiceProperties properties = bindProperties(
                    MqttDevServiceProperties.CONFIG_PREFIX, MqttDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServiceRegistry.ServiceSpec service) {
                    service
                            .name("mqtt")
                            .description("MQTT Dev Service")
                            .container(new java.util.function.Consumer<DevServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServiceRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseHiveMQContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseHiveMQContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty("mqtt.server.uri", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return mqttContainer().getBrokerUrl();
                }
            });
            addDynamicProperty("spring.mqtt.url", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return mqttContainer().getBrokerUrl();
                }
            });
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
