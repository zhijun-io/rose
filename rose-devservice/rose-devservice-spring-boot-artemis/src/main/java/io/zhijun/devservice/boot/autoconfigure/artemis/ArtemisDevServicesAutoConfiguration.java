package io.zhijun.devservice.boot.autoconfigure.artemis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * ActiveMQ Artemis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(ArtemisAutoConfiguration.class)
@ConditionalOnDevServiceEnabled("artemis")
@EnableConfigurationProperties(ArtemisDevServiceProperties.class)
@Import(ArtemisDevServicesAutoConfiguration.ArtemisDevServiceRegistrar.class)
public final class ArtemisDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<ArtemisDevServiceProperties, ArtemisContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<ArtemisDevServiceProperties, ArtemisContainer>builder()
                    .propertiesType(ArtemisDevServiceProperties.class)
                    .configPrefix(ArtemisDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("artemis")
                    .displayName("Artemis Dev Service")
                    .category(DevServiceCategories.JMS)
                    .containerClass(ArtemisContainer.class)
                    .containerFactory(ArtemisContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty("spring.artemis.broker-url",
                                () -> registrar.requireRunningContainer().getBrokerUrl());
                        registrar.addDynamicProperty("spring.artemis.user",
                                () -> registrar.requireRunningContainer().getUsername());
                        registrar.addDynamicProperty("spring.artemis.password",
                                () -> registrar.requireRunningContainer().getPassword());
                    })
                    .build();

    static final class ArtemisDevServiceRegistrar
            extends ContainerDevServiceRegistrar<ArtemisDevServiceProperties, ArtemisContainer> {

        ArtemisDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
