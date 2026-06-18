package io.zhijun.dev.services.activemq;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.services.api.provider.DevServiceCategories;
import io.zhijun.dev.services.api.provider.DevServiceProvider;
import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.activemq.ActiveMqDevServicesAutoConfiguration.ActiveMqDevServicesRegistrar;

/**
 * ActiveMQ Classic dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(ActiveMQAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("activemq")
@EnableConfigurationProperties(ActiveMqDevServicesProperties.class)
@Import(ActiveMqDevServicesRegistrar.class)
public final class ActiveMqDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider activeMqDevServiceProvider() {
        return DevServiceProvider.of("activemq", DevServiceCategories.JMS);
    }

    static class ActiveMqDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final ActiveMqDevServicesProperties properties = bindProperties(
                    ActiveMqDevServicesProperties.CONFIG_PREFIX, ActiveMqDevServicesProperties.class);

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
