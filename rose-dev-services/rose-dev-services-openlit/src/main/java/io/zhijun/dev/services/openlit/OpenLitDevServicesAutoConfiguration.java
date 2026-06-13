package io.zhijun.dev.services.openlit;

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
import io.zhijun.dev.services.openlit.OpenLitDevServicesAutoConfiguration.OpenLitDevServicesRegistrar;

/**
 * OpenLit dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@ConditionalOnDevServicesEnabled("openlit")
@EnableConfigurationProperties(OpenLitDevServicesProperties.class)
@Import(OpenLitDevServicesRegistrar.class)
public final class OpenLitDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider openLitDevServiceProvider() {
        return DevServiceProvider.of("openlit", DevServiceCategories.OPENTELEMETRY);
    }

    static class OpenLitDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final OpenLitDevServicesProperties properties = bindProperties(
                    OpenLitDevServicesProperties.CONFIG_PREFIX, OpenLitDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("openlit")
                            .description("OpenLit Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
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
            addDynamicProperty("rose.dev.services.openlit.ui-url", new java.util.function.Supplier<Object>() {
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
