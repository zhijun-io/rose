package io.zhijun.devservice.boot.autoconfigure.openlit;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;

/**
 * OpenLit dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(OpenLitDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(OpenLitDevServiceProperties.class)
@Import(OpenLitDevServicesAutoConfiguration.OpenLitDevServiceRegistrar.class)
public final class OpenLitDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<OpenLitDevServiceProperties, OpenLitContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<OpenLitDevServiceProperties, OpenLitContainer>builder()
                    .propertiesType(OpenLitDevServiceProperties.class)
                    .configPrefix(OpenLitDevServiceProperties.CONFIG_PREFIX)
                    .serviceName(OpenLitDevServiceProperties.SERVICE_NAME)
                    .displayName("OpenLit Dev Service")
                    .category(DevServiceCategory.OPENTELEMETRY)
                    .containerClass(OpenLitContainer.class)
                    .containerFactory(OpenLitContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty("OTEL_EXPORTER_OTLP_ENDPOINT",
                                () -> registrar.requireRunningContainer().getOtlpHttpUrl());
                        registrar.addDynamicProperty(OpenLitDevServiceProperties.UI_URL_PROPERTY,
                                () -> registrar.requireRunningContainer().getOpenLitUrl());
                    })
                    .build();

    static final class OpenLitDevServiceRegistrar
            extends ContainerDevServiceRegistrar<OpenLitDevServiceProperties, OpenLitContainer> {

        OpenLitDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
