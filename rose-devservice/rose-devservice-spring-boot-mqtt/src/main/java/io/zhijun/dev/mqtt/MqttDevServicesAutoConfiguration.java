package io.zhijun.dev.mqtt;

import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.api.provider.LocalServiceCategories;
import io.zhijun.dev.api.provider.LocalServiceProvider;
import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.registration.LocalServiceRegistrar;
import io.zhijun.dev.core.registration.LocalServiceRegistry;
import io.zhijun.dev.mqtt.MqttDevServicesAutoConfiguration.MqttLocalServiceRegistrar;

/**
 * MQTT dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("mqtt")
@EnableConfigurationProperties(MqttDevServiceProperties.class)
@Import(MqttLocalServiceRegistrar.class)
public final class MqttDevServicesAutoConfiguration {

    @Bean
    LocalServiceProvider mqttDevServiceProvider() {
        return LocalServiceProvider.of("mqtt", LocalServiceCategories.MQTT);
    }

    static class MqttLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final MqttDevServiceProperties properties = bindProperties(
                    MqttDevServiceProperties.CONFIG_PREFIX, MqttDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<LocalServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(LocalServiceRegistry.ServiceSpec service) {
                    service
                            .name("mqtt")
                            .description("MQTT Dev Service")
                            .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(LocalServiceRegistry.ContainerSpec container) {
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
