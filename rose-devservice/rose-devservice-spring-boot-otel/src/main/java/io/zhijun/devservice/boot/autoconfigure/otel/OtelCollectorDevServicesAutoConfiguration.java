package io.zhijun.devservice.boot.autoconfigure.otel;

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
import io.zhijun.devservice.boot.autoconfigure.otel.OtelCollectorDevServicesAutoConfiguration.OtelCollectorDevServiceRegistrar;

/**
 * OpenTelemetry Collector dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("otel-collector")
@EnableConfigurationProperties(OtelCollectorDevServiceProperties.class)
@Import(OtelCollectorDevServiceRegistrar.class)
public final class OtelCollectorDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider otelCollectorDevServiceProvider() {
        return DevServiceProvider.of("otel-collector", DevServiceCategories.OPENTELEMETRY);
    }

    static class OtelCollectorDevServiceRegistrar
            extends ContainerDevServiceRegistrar<OtelCollectorDevServiceProperties, RoseOtelCollectorContainer> {

        @Override
        protected Class<OtelCollectorDevServiceProperties> getPropertiesType() {
            return OtelCollectorDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return OtelCollectorDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "otel-collector";
        }

        @Override
        protected String getDisplayName() {
            return "OpenTelemetry Collector Dev Service";
        }

        @Override
        protected Class<RoseOtelCollectorContainer> getContainerClass() {
            return RoseOtelCollectorContainer.class;
        }

        @Override
        protected RoseOtelCollectorContainer createContainer(OtelCollectorDevServiceProperties properties) {
            return new RoseOtelCollectorContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("OTEL_EXPORTER_OTLP_ENDPOINT", () -> requireRunningContainer().getOtlpHttpUrl());
            addDynamicProperty("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT", () -> requireRunningContainer().getOtlpHttpUrl());
        }
    }
}
