package io.zhijun.devservice.boot.autoconfigure.kafka;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
import io.zhijun.devservice.boot.autoconfigure.kafka.KafkaDevServicesAutoConfiguration.KafkaDevServiceRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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

    static class KafkaDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            KafkaDevServiceProperties properties = bindProperties(
                    KafkaDevServiceProperties.CONFIG_PREFIX, KafkaDevServiceProperties.class);

            registry.registerDevService("kafka", "Kafka Dev Service",
                    RoseKafkaContainer.class, () -> new RoseKafkaContainer(properties));

            addDynamicProperty("spring.kafka.bootstrap-servers", () -> kafkaContainer().getBootstrapServers());
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
