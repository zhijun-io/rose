package io.zhijun.devservice.boot.autoconfigure.openlit;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.DevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceRegistry;
import io.zhijun.devservice.boot.autoconfigure.openlit.OpenLitDevServicesAutoConfiguration.OpenLitDevServiceRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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

    static class OpenLitDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            OpenLitDevServiceProperties properties = bindProperties(
                    OpenLitDevServiceProperties.CONFIG_PREFIX, OpenLitDevServiceProperties.class);

            registry.registerDevService("openlit", "OpenLit Dev Service",
                    RoseOpenLitContainer.class, () -> new RoseOpenLitContainer(properties));

            addDynamicProperty("OTEL_EXPORTER_OTLP_ENDPOINT", () -> openLitContainer().getOtlpHttpUrl());
            addDynamicProperty("rose.dev.openlit.ui-url", () -> openLitContainer().getOpenLitUrl());
        }

        private RoseOpenLitContainer openLitContainer() {
            RoseOpenLitContainer container = getBeanFactory().getBean(RoseOpenLitContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
