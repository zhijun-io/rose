package io.zhijun.devservice.boot.autoconfigure.activemq;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
import io.zhijun.devservice.boot.autoconfigure.activemq.ActiveMqDevServicesAutoConfiguration.ActiveMqDevServiceRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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

    static class ActiveMqDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            ActiveMqDevServiceProperties properties = bindProperties(
                    ActiveMqDevServiceProperties.CONFIG_PREFIX, ActiveMqDevServiceProperties.class);

            registry.registerDevService("activemq", "ActiveMQ Dev Service",
                    RoseActiveMqContainer.class, () -> new RoseActiveMqContainer(properties));

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
