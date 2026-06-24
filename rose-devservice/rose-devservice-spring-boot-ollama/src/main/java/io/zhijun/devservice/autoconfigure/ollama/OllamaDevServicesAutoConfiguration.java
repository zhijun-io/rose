package io.zhijun.devservice.autoconfigure.ollama;

import io.zhijun.devservice.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
import io.zhijun.devservice.autoconfigure.ollama.OllamaDevServicesAutoConfiguration.OllamaDevServiceRegistrar;

/**
 * Ollama dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("ollama")
@ConditionalOnOllamaNativeUnavailable
@EnableConfigurationProperties(OllamaDevServiceProperties.class)
@Import(OllamaDevServiceRegistrar.class)
public final class OllamaDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider ollamaDevServiceProvider() {
        return DevServiceProvider.of("ollama", DevServiceCategories.OLLAMA);
    }

    static class OllamaDevServiceRegistrar extends DevServiceRegistrar {

        private static final String OLLAMA_BASE_URL_PROPERTY = OllamaDevServiceProperties.BASE_URL_PROPERTY;

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            final OllamaDevServiceProperties properties = bindProperties(
                    OllamaDevServiceProperties.CONFIG_PREFIX, OllamaDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServiceRegistry.ServiceSpec service) {
                    service
                            .name("ollama")
                            .description("Ollama Dev Service")
                            .container(new java.util.function.Consumer<DevServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServiceRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseOllamaContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseOllamaContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty(OLLAMA_BASE_URL_PROPERTY, new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return ollamaContainer().getBaseUrl();
                }
            });
        }

        private RoseOllamaContainer ollamaContainer() {
            RoseOllamaContainer container = getBeanFactory().getBean(RoseOllamaContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
