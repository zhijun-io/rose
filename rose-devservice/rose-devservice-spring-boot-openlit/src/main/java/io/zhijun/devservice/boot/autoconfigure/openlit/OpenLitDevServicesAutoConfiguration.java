package io.zhijun.devservice.boot.autoconfigure.openlit;

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
import io.zhijun.devservice.boot.autoconfigure.openlit.OpenLitDevServicesAutoConfiguration.OpenLitDevServiceRegistrar;

/**
 * OpenLit dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("openlit")
@EnableConfigurationProperties(OpenLitDevServiceProperties.class)
@Import(OpenLitDevServiceRegistrar.class)
public final class OpenLitDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider openLitDevServiceProvider() {
        return DevServiceProvider.of("openlit", DevServiceCategories.OPENTELEMETRY);
    }

    static class OpenLitDevServiceRegistrar
            extends ContainerDevServiceRegistrar<OpenLitDevServiceProperties, RoseOpenLitContainer> {

        @Override
        protected Class<OpenLitDevServiceProperties> getPropertiesType() {
            return OpenLitDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return OpenLitDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "openlit";
        }

        @Override
        protected String getDisplayName() {
            return "OpenLit Dev Service";
        }

        @Override
        protected Class<RoseOpenLitContainer> getContainerClass() {
            return RoseOpenLitContainer.class;
        }

        @Override
        protected RoseOpenLitContainer createContainer(OpenLitDevServiceProperties properties) {
            return new RoseOpenLitContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("OTEL_EXPORTER_OTLP_ENDPOINT", () -> requireRunningContainer().getOtlpHttpUrl());
            addDynamicProperty("rose.dev.openlit.ui-url", () -> requireRunningContainer().getOpenLitUrl());
        }
    }
}
