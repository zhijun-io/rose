package io.zhijun.dev.services.mqtt;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.services.api.provider.DevServiceCategories;
import io.zhijun.dev.services.api.provider.DevServiceProvider;
import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.mqtt.MqttDevServicesAutoConfiguration.MqttDevServicesRegistrar;

/**
 * MQTT dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("mqtt")
@EnableConfigurationProperties(MqttDevServicesProperties.class)
@Import(MqttDevServicesRegistrar.class)
public final class MqttDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider mqttDevServiceProvider() {
        return DevServiceProvider.of("mqtt", DevServiceCategories.MQTT);
    }

    static class MqttDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final MqttDevServicesProperties properties = bindProperties(
                    MqttDevServicesProperties.CONFIG_PREFIX, MqttDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("mqtt")
                            .description("MQTT Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
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
