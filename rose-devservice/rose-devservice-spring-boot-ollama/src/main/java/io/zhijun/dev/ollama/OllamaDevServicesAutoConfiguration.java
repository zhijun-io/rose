package io.zhijun.dev.ollama;

import io.zhijun.dev.core.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.registration.DevServiceRegistrar;
import io.zhijun.dev.core.registration.DevServiceRegistry;
import io.zhijun.dev.ollama.OllamaDevServicesAutoConfiguration.OllamaDevServiceRegistrar;

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
