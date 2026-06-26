package io.zhijun.devservice.boot.autoconfigure.openlit;

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
 * OpenLit dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("openlit")
@EnableConfigurationProperties(OpenLitDevServiceProperties.class)
@Import(OpenLitDevServicesAutoConfiguration.OpenLitDevServiceRegistrar.class)
public final class OpenLitDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<OpenLitDevServiceProperties, OpenLitContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<OpenLitDevServiceProperties, OpenLitContainer>builder()
                    .propertiesType(OpenLitDevServiceProperties.class)
                    .configPrefix(OpenLitDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("openlit")
                    .displayName("OpenLit Dev Service")
                    .category(DevServiceCategories.OPENTELEMETRY)
                    .containerClass(OpenLitContainer.class)
                    .containerFactory(OpenLitContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty("OTEL_EXPORTER_OTLP_ENDPOINT",
                                () -> registrar.requireRunningContainer().getOtlpHttpUrl());
                        registrar.addDynamicProperty("rose.dev.openlit.ui-url",
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
