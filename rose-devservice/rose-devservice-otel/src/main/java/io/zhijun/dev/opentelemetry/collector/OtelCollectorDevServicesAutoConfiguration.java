package io.zhijun.dev.opentelemetry.collector;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.api.provider.LocalServiceCategories;
import io.zhijun.dev.api.provider.LocalServiceProvider;
import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;
import io.zhijun.dev.core.registration.LocalServiceRegistrar;
import io.zhijun.dev.core.registration.LocalServiceRegistry;
import io.zhijun.dev.opentelemetry.collector.OtelCollectorDevServicesAutoConfiguration.OtelCollectorLocalServiceRegistrar;

/**
 * OpenTelemetry Collector dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("otel-collector")
@EnableConfigurationProperties(OtelCollectorDevServiceProperties.class)
@Import(OtelCollectorLocalServiceRegistrar.class)
public final class OtelCollectorDevServicesAutoConfiguration {

    @Bean
    LocalServiceProvider otelCollectorDevServiceProvider() {
        return LocalServiceProvider.of("otel-collector", LocalServiceCategories.OPENTELEMETRY);
    }

    static class OtelCollectorLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final OtelCollectorDevServiceProperties properties = bindProperties(
                    OtelCollectorDevServiceProperties.CONFIG_PREFIX, OtelCollectorDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<LocalServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(LocalServiceRegistry.ServiceSpec service) {
                    service
                            .name("otel-collector")
                            .description("OpenTelemetry Collector Dev Service")
                            .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(LocalServiceRegistry.ContainerSpec container) {
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
