package io.zhijun.devservice.boot.autoconfigure.ollama;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * Ollama dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("ollama")
@EnableConfigurationProperties(OllamaDevServiceProperties.class)
@Import(OllamaDevServicesAutoConfiguration.OllamaDevServiceRegistrar.class)
public final class OllamaDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<OllamaDevServiceProperties, OllamaContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<OllamaDevServiceProperties, OllamaContainer>builder()
                    .propertiesType(OllamaDevServiceProperties.class)
                    .configPrefix(OllamaDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("ollama")
                    .displayName("Ollama Dev Service")
                    .category(DevServiceCategories.OLLAMA)
                    .containerClass(OllamaContainer.class)
                    .containerFactory(OllamaContainer::new)
                    .dynamicProperties(registrar -> registrar.addDynamicProperty(
                            OllamaDevServiceProperties.BASE_URL_PROPERTY,
                            () -> registrar.requireRunningContainer().getBaseUrl()))
                    .build();

    static final class OllamaDevServiceRegistrar
            extends ContainerDevServiceRegistrar<OllamaDevServiceProperties, OllamaContainer> {

        OllamaDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
