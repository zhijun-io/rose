package io.zhijun.dev.openlit;

import io.zhijun.dev.core.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.api.provider.DevServiceCategories;
import io.zhijun.dev.api.provider.DevServiceProvider;
import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.registration.DevServiceRegistrar;
import io.zhijun.dev.core.registration.DevServiceRegistry;
import io.zhijun.dev.openlit.OpenLitDevServicesAutoConfiguration.OpenLitDevServiceRegistrar;

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
            final OpenLitDevServiceProperties properties = bindProperties(
                    OpenLitDevServiceProperties.CONFIG_PREFIX, OpenLitDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServiceRegistry.ServiceSpec service) {
                    service
                            .name("openlit")
                            .description("OpenLit Dev Service")
                            .container(new java.util.function.Consumer<DevServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServiceRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseOpenLitContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseOpenLitContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty("OTEL_EXPORTER_OTLP_ENDPOINT", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return openLitContainer().getOtlpHttpUrl();
                }
            });
            addDynamicProperty("rose.dev.openlit.ui-url", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return openLitContainer().getOpenLitUrl();
                }
            });
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
