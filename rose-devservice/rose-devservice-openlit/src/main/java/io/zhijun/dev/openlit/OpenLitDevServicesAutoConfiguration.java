package io.zhijun.dev.openlit;

import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.api.provider.LocalServiceCategories;
import io.zhijun.dev.api.provider.LocalServiceProvider;
import io.zhijun.dev.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.dev.core.registration.LocalServiceRegistrar;
import io.zhijun.dev.core.registration.LocalServiceRegistry;
import io.zhijun.dev.openlit.OpenLitDevServicesAutoConfiguration.OpenLitLocalServiceRegistrar;

/**
 * OpenLit dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("openlit")
@EnableConfigurationProperties(OpenLitDevServiceProperties.class)
@Import(OpenLitLocalServiceRegistrar.class)
public final class OpenLitDevServicesAutoConfiguration {

    @Bean
    LocalServiceProvider openLitDevServiceProvider() {
        return LocalServiceProvider.of("openlit", LocalServiceCategories.OPENTELEMETRY);
    }

    static class OpenLitLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final OpenLitDevServiceProperties properties = bindProperties(
                    OpenLitDevServiceProperties.CONFIG_PREFIX, OpenLitDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<LocalServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(LocalServiceRegistry.ServiceSpec service) {
                    service
                            .name("openlit")
                            .description("OpenLit Dev Service")
                            .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(LocalServiceRegistry.ContainerSpec container) {
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
            addDynamicProperty("rose.local.openlit.ui-url", new java.util.function.Supplier<Object>() {
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
