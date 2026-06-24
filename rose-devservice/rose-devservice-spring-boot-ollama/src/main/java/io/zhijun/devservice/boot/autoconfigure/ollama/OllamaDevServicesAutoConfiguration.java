package io.zhijun.devservice.boot.autoconfigure.ollama;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
import io.zhijun.devservice.boot.autoconfigure.ollama.OllamaDevServicesAutoConfiguration.OllamaDevServiceRegistrar;

/**
 * Ollama dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("ollama")
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
            OllamaDevServiceProperties properties = bindProperties(
                    OllamaDevServiceProperties.CONFIG_PREFIX, OllamaDevServiceProperties.class);

            registry.registerDevService("ollama", "Ollama Dev Service",
                    RoseOllamaContainer.class, () -> new RoseOllamaContainer(properties));

            addDynamicProperty(OLLAMA_BASE_URL_PROPERTY, () -> ollamaContainer().getBaseUrl());
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
