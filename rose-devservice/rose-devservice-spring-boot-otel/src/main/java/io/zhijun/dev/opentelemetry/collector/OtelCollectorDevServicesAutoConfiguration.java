package io.zhijun.dev.opentelemetry.collector;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.api.provider.DevServiceCategories;
import io.zhijun.dev.api.provider.DevServiceProvider;
import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.dev.core.registration.DevServiceRegistrar;
import io.zhijun.dev.core.registration.DevServiceRegistry;
import io.zhijun.dev.opentelemetry.collector.OtelCollectorDevServicesAutoConfiguration.OtelCollectorDevServiceRegistrar;

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

    static class OtelCollectorDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            final OtelCollectorDevServiceProperties properties = bindProperties(
                    OtelCollectorDevServiceProperties.CONFIG_PREFIX, OtelCollectorDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServiceRegistry.ServiceSpec service) {
                    service
                            .name("otel-collector")
                            .description("OpenTelemetry Collector Dev Service")
                            .container(new java.util.function.Consumer<DevServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServiceRegistry.ContainerSpec container) {
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
