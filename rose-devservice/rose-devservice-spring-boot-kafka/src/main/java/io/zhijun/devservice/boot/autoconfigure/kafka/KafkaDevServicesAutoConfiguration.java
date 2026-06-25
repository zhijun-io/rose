package io.zhijun.devservice.boot.autoconfigure.kafka;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * Kafka dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(KafkaAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("kafka")
@EnableConfigurationProperties(KafkaDevServiceProperties.class)
@Import(KafkaDevServicesAutoConfiguration.KafkaDevServiceRegistrar.class)
public final class KafkaDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<KafkaDevServiceProperties, RoseKafkaContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<KafkaDevServiceProperties, RoseKafkaContainer>builder()
                    .propertiesType(KafkaDevServiceProperties.class)
                    .configPrefix(KafkaDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("kafka")
                    .displayName("Kafka Dev Service")
                    .category(DevServiceCategories.KAFKA)
                    .containerClass(RoseKafkaContainer.class)
                    .containerFactory(RoseKafkaContainer::new)
                    .dynamicProperties(registrar -> registrar.addDynamicProperty("spring.kafka.bootstrap-servers",
                            () -> registrar.requireRunningContainer().getBootstrapServers()))
                    .build();

    static final class KafkaDevServiceRegistrar
            extends ContainerDevServiceRegistrar<KafkaDevServiceProperties, RoseKafkaContainer> {

        KafkaDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
