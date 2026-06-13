package io.zhijun.dev.services.opentelemetry.collector;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.services.api.provider.DevServiceCategories;
import io.zhijun.dev.services.api.provider.DevServiceProvider;
import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.opentelemetry.collector.OtelCollectorDevServicesAutoConfiguration.OtelCollectorDevServicesRegistrar;

/**
 * OpenTelemetry Collector dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("otel-collector")
@EnableConfigurationProperties(OtelCollectorDevServicesProperties.class)
@Import(OtelCollectorDevServicesRegistrar.class)
public final class OtelCollectorDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider otelCollectorDevServiceProvider() {
        return DevServiceProvider.of("otel-collector", DevServiceCategories.OPENTELEMETRY);
    }

    static class OtelCollectorDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final OtelCollectorDevServicesProperties properties = bindProperties(
                    OtelCollectorDevServicesProperties.CONFIG_PREFIX, OtelCollectorDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("otel-collector")
                            .description("OpenTelemetry Collector Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseOtelCollectorContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseOtelCollectorContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty("OTEL_EXPORTER_OTLP_ENDPOINT", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return otelCollector().getOtlpHttpUrl();
                }
            });
            addDynamicProperty("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return otelCollector().getOtlpHttpUrl();
                }
            });
        }

        private RoseOtelCollectorContainer otelCollector() {
            RoseOtelCollectorContainer container = getBeanFactory().getBean(RoseOtelCollectorContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
