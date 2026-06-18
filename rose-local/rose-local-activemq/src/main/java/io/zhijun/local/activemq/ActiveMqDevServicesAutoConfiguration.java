package io.zhijun.local.activemq;

import io.zhijun.local.core.autoconfigure.LocalServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.local.api.provider.LocalServiceCategories;
import io.zhijun.local.api.provider.LocalServiceProvider;
import io.zhijun.local.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.local.core.registration.LocalServiceRegistrar;
import io.zhijun.local.core.registration.LocalServiceRegistry;
import io.zhijun.local.activemq.ActiveMqDevServicesAutoConfiguration.ActiveMqLocalServiceRegistrar;

/**
 * ActiveMQ Classic dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(ActiveMQAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("activemq")
@EnableConfigurationProperties(ActiveMqLocalServiceProperties.class)
@Import(ActiveMqLocalServiceRegistrar.class)
public final class ActiveMqDevServicesAutoConfiguration {

    @Bean
    LocalServiceProvider activeMqDevServiceProvider() {
        return LocalServiceProvider.of("activemq", LocalServiceCategories.JMS);
    }

    static class ActiveMqLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final ActiveMqLocalServiceProperties properties = bindProperties(
                    ActiveMqLocalServiceProperties.CONFIG_PREFIX, ActiveMqLocalServiceProperties.class);

            registry.registerDevService(service -> service
                    .name("activemq")
                    .description("ActiveMQ Classic Dev Service")
                    .container(container -> container
                            .type(RoseActiveMqContainer.class)
                            .supplier(() -> new RoseActiveMqContainer(properties))));

            addDynamicProperty("spring.activemq.broker-url", () -> activeMqContainer().getBrokerUrl());
            addDynamicProperty("spring.activemq.user", () -> activeMqContainer().getUsername());
            addDynamicProperty("spring.activemq.password", () -> activeMqContainer().getPassword());
        }

        private RoseActiveMqContainer activeMqContainer() {
            RoseActiveMqContainer container = getBeanFactory().getBean(RoseActiveMqContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
