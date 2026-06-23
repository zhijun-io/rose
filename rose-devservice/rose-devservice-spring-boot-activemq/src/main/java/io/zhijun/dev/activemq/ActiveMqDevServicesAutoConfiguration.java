package io.zhijun.dev.activemq;

import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
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
import io.zhijun.dev.activemq.ActiveMqDevServicesAutoConfiguration.ActiveMqLocalServiceRegistrar;

/**
 * ActiveMQ Classic dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(ActiveMQAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("activemq")
@EnableConfigurationProperties(ActiveMqDevServiceProperties.class)
@Import(ActiveMqLocalServiceRegistrar.class)
public final class ActiveMqDevServicesAutoConfiguration {

    @Bean
    LocalServiceProvider activeMqDevServiceProvider() {
        return LocalServiceProvider.of("activemq", LocalServiceCategories.JMS);
    }

    static class ActiveMqLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final ActiveMqDevServiceProperties properties = bindProperties(
                    ActiveMqDevServiceProperties.CONFIG_PREFIX, ActiveMqDevServiceProperties.class);

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
