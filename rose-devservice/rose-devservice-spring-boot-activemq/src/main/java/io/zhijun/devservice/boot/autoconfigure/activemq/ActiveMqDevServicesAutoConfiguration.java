package io.zhijun.devservice.boot.autoconfigure.activemq;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.autoconfigure.activemq.ActiveMqDevServicesAutoConfiguration.ActiveMqDevServiceRegistrar;

/**
 * ActiveMQ Classic dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(ActiveMQAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("activemq")
@EnableConfigurationProperties(ActiveMqDevServiceProperties.class)
@Import(ActiveMqDevServiceRegistrar.class)
public final class ActiveMqDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider activeMqDevServiceProvider() {
        return DevServiceProvider.of("activemq", DevServiceCategories.JMS);
    }

    static class ActiveMqDevServiceRegistrar
            extends ContainerDevServiceRegistrar<ActiveMqDevServiceProperties, RoseActiveMqContainer> {

        @Override
        protected Class<ActiveMqDevServiceProperties> getPropertiesType() {
            return ActiveMqDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return ActiveMqDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "activemq";
        }

        @Override
        protected String getDisplayName() {
            return "ActiveMQ Dev Service";
        }

        @Override
        protected Class<RoseActiveMqContainer> getContainerClass() {
            return RoseActiveMqContainer.class;
        }

        @Override
        protected RoseActiveMqContainer createContainer(ActiveMqDevServiceProperties properties) {
            return new RoseActiveMqContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("spring.activemq.broker-url", () -> requireRunningContainer().getBrokerUrl());
            addDynamicProperty("spring.activemq.user", () -> requireRunningContainer().getUsername());
            addDynamicProperty("spring.activemq.password", () -> requireRunningContainer().getPassword());
        }
    }
}
