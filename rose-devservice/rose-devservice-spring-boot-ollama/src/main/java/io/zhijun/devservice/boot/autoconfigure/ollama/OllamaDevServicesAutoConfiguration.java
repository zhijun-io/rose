package io.zhijun.devservice.boot.autoconfigure.ollama;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.DevServiceAutoConfigRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Ollama dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(OllamaDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(OllamaDevServiceProperties.class)
@Import(DevServiceAutoConfigRegistrar.class)
public final class OllamaDevServicesAutoConfiguration {

    static final DevServiceConnectorDescriptor<OllamaDevServiceProperties, OllamaContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<OllamaDevServiceProperties, OllamaContainer>builder()
                    .propertiesType(OllamaDevServiceProperties.class)
                    .configPrefix(OllamaDevServiceProperties.CONFIG_PREFIX)
                    .serviceName(OllamaDevServiceProperties.SERVICE_NAME)
                    .displayName("Ollama Dev Service")
                    .category(DevServiceCategory.OLLAMA)
                    .containerClass(OllamaContainer.class)
                    .containerFactory(OllamaContainer::new)
                    .dynamicProperties(registrar -> registrar.addDynamicProperty(
                            OllamaDevServiceProperties.BASE_URL_PROPERTY,
                            () -> registrar.requireRunningContainer().getBaseUrl()))
                    .build();
}
