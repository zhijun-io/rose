package io.zhijun.dev.ollama;

import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.registration.LocalServiceRegistrar;
import io.zhijun.dev.core.registration.LocalServiceRegistry;
import io.zhijun.dev.ollama.OllamaDevServicesAutoConfiguration.OllamaLocalServiceRegistrar;

/**
 * Ollama dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("ollama")
@ConditionalOnOllamaNativeUnavailable
@EnableConfigurationProperties(OllamaDevServiceProperties.class)
@Import(OllamaLocalServiceRegistrar.class)
public final class OllamaDevServicesAutoConfiguration {

    static class OllamaLocalServiceRegistrar extends LocalServiceRegistrar {

        private static final String OLLAMA_BASE_URL_PROPERTY = OllamaDevServiceProperties.BASE_URL_PROPERTY;

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final OllamaDevServiceProperties properties = bindProperties(
                    OllamaDevServiceProperties.CONFIG_PREFIX, OllamaDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<LocalServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(LocalServiceRegistry.ServiceSpec service) {
                    service
                            .name("ollama")
                            .description("Ollama Dev Service")
                            .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(LocalServiceRegistry.ContainerSpec container) {
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
