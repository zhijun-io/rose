package io.zhijun.dev.services.artemis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
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
import io.zhijun.dev.services.artemis.ArtemisDevServicesAutoConfiguration.ArtemisDevServicesRegistrar;

/**
 * ActiveMQ Artemis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(ArtemisAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("artemis")
@EnableConfigurationProperties(ArtemisDevServicesProperties.class)
@Import(ArtemisDevServicesRegistrar.class)
public final class ArtemisDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider artemisDevServiceProvider() {
        return DevServiceProvider.of("artemis", DevServiceCategories.JMS);
    }

    static class ArtemisDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final ArtemisDevServicesProperties properties = bindProperties(
                    ArtemisDevServicesProperties.CONFIG_PREFIX, ArtemisDevServicesProperties.class);

            registry.registerDevService(service -> service
                    .name("artemis")
                    .description("ActiveMQ Artemis Dev Service")
                    .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                        @Override
                        public void accept(DevServicesRegistry.ContainerSpec container) {
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

            setDefaultProperty("spring.artemis.mode", "native");
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
