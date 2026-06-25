package io.zhijun.devservice.boot.autoconfigure.ollama;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
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

    static class OllamaDevServiceRegistrar
            extends ContainerDevServiceRegistrar<OllamaDevServiceProperties, RoseOllamaContainer> {

        @Override
        protected Class<OllamaDevServiceProperties> getPropertiesType() {
            return OllamaDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return OllamaDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "ollama";
        }

        @Override
        protected String getDisplayName() {
            return "Ollama Dev Service";
        }

        @Override
        protected Class<RoseOllamaContainer> getContainerClass() {
            return RoseOllamaContainer.class;
        }

        @Override
        protected RoseOllamaContainer createContainer(OllamaDevServiceProperties properties) {
            return new RoseOllamaContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty(OllamaDevServiceProperties.BASE_URL_PROPERTY,
                    () -> requireRunningContainer().getBaseUrl());
        }
    }
}
