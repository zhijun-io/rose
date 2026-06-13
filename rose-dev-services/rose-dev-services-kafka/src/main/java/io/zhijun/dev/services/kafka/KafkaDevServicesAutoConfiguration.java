package io.zhijun.dev.services.kafka;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.kafka.KafkaDevServicesAutoConfiguration.KafkaDevServicesRegistrar;

/**
 * Kafka dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(KafkaAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("kafka")
@EnableConfigurationProperties(KafkaDevServicesProperties.class)
@Import(KafkaDevServicesRegistrar.class)
public final class KafkaDevServicesAutoConfiguration {

    static class KafkaDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final KafkaDevServicesProperties properties = bindProperties(
                    KafkaDevServicesProperties.CONFIG_PREFIX, KafkaDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("kafka")
                            .description("Kafka Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
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
