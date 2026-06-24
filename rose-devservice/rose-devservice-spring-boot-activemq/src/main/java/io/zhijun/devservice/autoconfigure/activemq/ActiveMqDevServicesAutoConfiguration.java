package io.zhijun.devservice.autoconfigure.activemq;

import io.zhijun.devservice.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.api.provider.DevServiceCategories;
import io.zhijun.devservice.api.provider.DevServiceProvider;
import io.zhijun.devservice.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.registration.DevServiceRegistrar;
import io.zhijun.devservice.registration.DevServiceRegistry;
import io.zhijun.devservice.autoconfigure.activemq.ActiveMqDevServicesAutoConfiguration.ActiveMqDevServiceRegistrar;

/**
 * ActiveMQ Classic dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(ActiveMQAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("activemq")
@EnableConfigurationProperties(ActiveMqDevServiceProperties.class)
@Import(ActiveMqDevServiceRegistrar.class)
public final class ActiveMqDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider activeMqDevServiceProvider() {
        return DevServiceProvider.of("activemq", DevServiceCategories.JMS);
    }

    static class ActiveMqDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
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
