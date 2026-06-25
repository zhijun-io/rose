package io.zhijun.devservice.boot.autoconfigure.artemis;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.autoconfigure.artemis.ArtemisDevServicesAutoConfiguration.ArtemisDevServiceRegistrar;

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

    static class ArtemisDevServiceRegistrar
            extends ContainerDevServiceRegistrar<ArtemisDevServiceProperties, RoseArtemisContainer> {

        @Override
        protected Class<ArtemisDevServiceProperties> getPropertiesType() {
            return ArtemisDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return ArtemisDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "artemis";
        }

        @Override
        protected String getDisplayName() {
            return "Artemis Dev Service";
        }

        @Override
        protected Class<RoseArtemisContainer> getContainerClass() {
            return RoseArtemisContainer.class;
        }

        @Override
        protected RoseArtemisContainer createContainer(ArtemisDevServiceProperties properties) {
            return new RoseArtemisContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("spring.artemis.broker-url", () -> requireRunningContainer().getBrokerUrl());
            addDynamicProperty("spring.artemis.user", () -> requireRunningContainer().getUsername());
            addDynamicProperty("spring.artemis.password", () -> requireRunningContainer().getPassword());
        }
    }
}
