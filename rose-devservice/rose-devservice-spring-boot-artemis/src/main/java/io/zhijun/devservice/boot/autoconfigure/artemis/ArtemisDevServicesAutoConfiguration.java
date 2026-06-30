package io.zhijun.devservice.boot.autoconfigure.artemis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.boot.registration.DevServiceAutoConfigRegistrar;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;

/**
 * ActiveMQ Artemis dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@AutoConfigureBefore(ArtemisAutoConfiguration.class)
@ConditionalOnDevServiceEnabled(ArtemisDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(ArtemisDevServiceProperties.class)
@Import(DevServiceAutoConfigRegistrar.class)
public final class ArtemisDevServicesAutoConfiguration {

    static final DevServiceConnectorDescriptor<ArtemisDevServiceProperties, ArtemisContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<ArtemisDevServiceProperties, ArtemisContainer>builder()
                    .propertiesType(ArtemisDevServiceProperties.class)
                    .configPrefix(ArtemisDevServiceProperties.CONFIG_PREFIX)
                    .serviceName(ArtemisDevServiceProperties.SERVICE_NAME)
                    .displayName("Artemis Dev Service")
                    .category(DevServiceCategory.JMS)
                    .containerClass(ArtemisContainer.class)
                    .containerFactory(ArtemisContainer::new)
                    .dynamicProperties(registrar -> {
                        registrar.addDynamicProperty(
                                "spring.artemis.broker-url",
                                () -> registrar.requireRunningContainer().getBrokerUrl());
                        registrar.addDynamicProperty(
                                "spring.artemis.user",
                                () -> registrar.requireRunningContainer().getUsername());
                        registrar.addDynamicProperty(
                                "spring.artemis.password",
                                () -> registrar.requireRunningContainer().getPassword());
                    })
                    .build();
}
