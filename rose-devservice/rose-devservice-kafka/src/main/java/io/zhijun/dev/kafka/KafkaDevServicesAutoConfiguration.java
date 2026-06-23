package io.zhijun.dev.kafka;

import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.registration.LocalServiceRegistrar;
import io.zhijun.dev.core.registration.LocalServiceRegistry;
import io.zhijun.dev.kafka.KafkaDevServicesAutoConfiguration.KafkaLocalServiceRegistrar;

/**
 * Kafka dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(KafkaAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("kafka")
@EnableConfigurationProperties(KafkaDevServiceProperties.class)
@Import(KafkaLocalServiceRegistrar.class)
public final class KafkaDevServicesAutoConfiguration {

    static class KafkaLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final KafkaDevServiceProperties properties = bindProperties(
                    KafkaDevServiceProperties.CONFIG_PREFIX, KafkaDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<LocalServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(LocalServiceRegistry.ServiceSpec service) {
                    service
                            .name("kafka")
                            .description("Kafka Dev Service")
                            .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(LocalServiceRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseKafkaContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseKafkaContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty("spring.kafka.bootstrap-servers", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return kafkaContainer().getBootstrapServers();
                }
            });
        }

        private RoseKafkaContainer kafkaContainer() {
            RoseKafkaContainer container = getBeanFactory().getBean(RoseKafkaContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
