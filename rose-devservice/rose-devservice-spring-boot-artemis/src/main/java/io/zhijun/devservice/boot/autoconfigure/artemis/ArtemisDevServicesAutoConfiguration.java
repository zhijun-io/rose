package io.zhijun.devservice.boot.autoconfigure.artemis;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
import io.zhijun.devservice.boot.autoconfigure.artemis.ArtemisDevServicesAutoConfiguration.ArtemisDevServiceRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ActiveMQ Artemis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(ArtemisAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("artemis")
@EnableConfigurationProperties(ArtemisDevServiceProperties.class)
@Import(ArtemisDevServiceRegistrar.class)
public final class ArtemisDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider artemisDevServiceProvider() {
        return DevServiceProvider.of("artemis", DevServiceCategories.JMS);
    }

    static class ArtemisDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            ArtemisDevServiceProperties properties = bindProperties(
                    ArtemisDevServiceProperties.CONFIG_PREFIX, ArtemisDevServiceProperties.class);

            registry.registerDevService("artemis", "Artemis Dev Service",
                    RoseArtemisContainer.class, () -> new RoseArtemisContainer(properties));

            addDynamicProperty("spring.artemis.broker-url", () -> artemisContainer().getBrokerUrl());
            addDynamicProperty("spring.artemis.user", () -> artemisContainer().getUsername());
            addDynamicProperty("spring.artemis.password", () -> artemisContainer().getPassword());
        }

        private RoseArtemisContainer artemisContainer() {
            RoseArtemisContainer container = getBeanFactory().getBean(RoseArtemisContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
