package io.zhijun.local.artemis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.local.api.provider.LocalServiceCategories;
import io.zhijun.local.api.provider.LocalServiceProvider;
import io.zhijun.local.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.local.core.autoconfigure.LocalServiceAutoConfiguration;
import io.zhijun.local.core.registration.LocalServiceRegistrar;
import io.zhijun.local.core.registration.LocalServiceRegistry;
import io.zhijun.local.artemis.ArtemisDevServicesAutoConfiguration.ArtemisLocalServiceRegistrar;

/**
 * ActiveMQ Artemis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(ArtemisAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("artemis")
@EnableConfigurationProperties(ArtemisLocalServiceProperties.class)
@Import(ArtemisLocalServiceRegistrar.class)
public final class ArtemisDevServicesAutoConfiguration {

    @Bean
    LocalServiceProvider artemisDevServiceProvider() {
        return LocalServiceProvider.of("artemis", LocalServiceCategories.JMS);
    }

    static class ArtemisLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final ArtemisLocalServiceProperties properties = bindProperties(
                    ArtemisLocalServiceProperties.CONFIG_PREFIX, ArtemisLocalServiceProperties.class);

            registry.registerDevService(service -> service
                    .name("artemis")
                    .description("ActiveMQ Artemis Dev Service")
                    .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                        @Override
                        public void accept(LocalServiceRegistry.ContainerSpec container) {
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
