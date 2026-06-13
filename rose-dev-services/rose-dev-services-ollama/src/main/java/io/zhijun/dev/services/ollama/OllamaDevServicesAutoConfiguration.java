package io.zhijun.dev.services.ollama;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.ollama.OllamaDevServicesAutoConfiguration.OllamaDevServicesRegistrar;

/**
 * Ollama dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("ollama")
@ConditionalOnOllamaNativeUnavailable
@EnableConfigurationProperties(OllamaDevServicesProperties.class)
@Import(OllamaDevServicesRegistrar.class)
public final class OllamaDevServicesAutoConfiguration {

    static class OllamaDevServicesRegistrar extends DevServicesRegistrar {

        private static final String OLLAMA_BASE_URL_PROPERTY = OllamaDevServicesProperties.BASE_URL_PROPERTY;

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final OllamaDevServicesProperties properties = bindProperties(
                    OllamaDevServicesProperties.CONFIG_PREFIX, OllamaDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("ollama")
                            .description("Ollama Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
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
