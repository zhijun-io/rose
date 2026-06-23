package io.zhijun.devservice.artemis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.api.provider.DevServiceCategories;
import io.zhijun.devservice.api.provider.DevServiceProvider;
import io.zhijun.devservice.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.core.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.core.registration.DevServiceRegistrar;
import io.zhijun.devservice.core.registration.DevServiceRegistry;
import io.zhijun.devservice.artemis.ArtemisDevServicesAutoConfiguration.ArtemisDevServiceRegistrar;

/**
 * ActiveMQ Artemis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(ArtemisAutoConfiguration.class)
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
            final ArtemisDevServiceProperties properties = bindProperties(
                    ArtemisDevServiceProperties.CONFIG_PREFIX, ArtemisDevServiceProperties.class);

            registry.registerDevService(service -> service
                    .name("artemis")
                    .description("ActiveMQ Artemis Dev Service")
                    .container(new java.util.function.Consumer<DevServiceRegistry.ContainerSpec>() {
                        @Override
                        public void accept(DevServiceRegistry.ContainerSpec container) {
                            container
                                    .type(RoseArtemisContainer.class)
                                    .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                        @Override
                                        public org.testcontainers.containers.Container<?> get() {
                                            return new RoseArtemisContainer(properties);
                                        }
                                    });
                        }
                    }));

            addDynamicProperty("spring.artemis.broker-url", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return artemisContainer().getBrokerUrl();
                }
            });
            addDynamicProperty("spring.artemis.user", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return artemisContainer().getUsername();
                }
            });
            addDynamicProperty("spring.artemis.password", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return artemisContainer().getPassword();
                }
            });
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
