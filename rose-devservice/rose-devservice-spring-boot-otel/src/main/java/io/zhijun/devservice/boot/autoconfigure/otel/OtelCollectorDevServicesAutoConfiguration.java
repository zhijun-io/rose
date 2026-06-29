package io.zhijun.devservice.boot.autoconfigure.otel;

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
 * OpenTelemetry Collector dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(OtelCollectorDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(OtelCollectorDevServiceProperties.class)
@Import(OtelCollectorDevServicesAutoConfiguration.OtelCollectorDevServiceRegistrar.class)
public final class OtelCollectorDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<OtelCollectorDevServiceProperties, OtelCollectorContainer>
            DESCRIPTOR =
                    DevServiceConnectorDescriptor.<OtelCollectorDevServiceProperties, OtelCollectorContainer>builder()
                            .propertiesType(OtelCollectorDevServiceProperties.class)
                            .configPrefix(OtelCollectorDevServiceProperties.CONFIG_PREFIX)
                            .serviceName(OtelCollectorDevServiceProperties.SERVICE_NAME)
                            .displayName("OpenTelemetry Collector Dev Service")
                            .category(DevServiceCategory.OPENTELEMETRY)
                            .containerClass(OtelCollectorContainer.class)
                            .containerFactory(OtelCollectorContainer::new)
                            .dynamicProperties(registrar -> {
                                registrar.addDynamicProperty(
                                        "OTEL_EXPORTER_OTLP_ENDPOINT",
                                        () -> registrar
                                                .requireRunningContainer()
                                                .getOtlpHttpUrl());
                                registrar.addDynamicProperty(
                                        "OTEL_EXPORTER_OTLP_TRACES_ENDPOINT",
                                        () -> registrar
                                                .requireRunningContainer()
                                                .getOtlpHttpUrl());
                            })
                            .build();

    static final class OtelCollectorDevServiceRegistrar
            extends ContainerDevServiceRegistrar<OtelCollectorDevServiceProperties, OtelCollectorContainer> {

        OtelCollectorDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
