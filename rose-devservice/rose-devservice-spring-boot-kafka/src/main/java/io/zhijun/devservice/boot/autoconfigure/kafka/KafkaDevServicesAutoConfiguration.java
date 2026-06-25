package io.zhijun.devservice.boot.autoconfigure.kafka;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.autoconfigure.kafka.KafkaDevServicesAutoConfiguration.KafkaDevServiceRegistrar;

/**
 * Kafka dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(KafkaAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("kafka")
@EnableConfigurationProperties(KafkaDevServiceProperties.class)
@Import(KafkaDevServiceRegistrar.class)
public final class KafkaDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider kafkaDevServiceProvider() {
        return DevServiceProvider.of("kafka", DevServiceCategories.KAFKA);
    }

    static class KafkaDevServiceRegistrar
            extends ContainerDevServiceRegistrar<KafkaDevServiceProperties, RoseKafkaContainer> {

        @Override
        protected Class<KafkaDevServiceProperties> getPropertiesType() {
            return KafkaDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return KafkaDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "kafka";
        }

        @Override
        protected String getDisplayName() {
            return "Kafka Dev Service";
        }

        @Override
        protected Class<RoseKafkaContainer> getContainerClass() {
            return RoseKafkaContainer.class;
        }

        @Override
        protected RoseKafkaContainer createContainer(KafkaDevServiceProperties properties) {
            return new RoseKafkaContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("spring.kafka.bootstrap-servers",
                    () -> requireRunningContainer().getBootstrapServers());
        }
    }
}
