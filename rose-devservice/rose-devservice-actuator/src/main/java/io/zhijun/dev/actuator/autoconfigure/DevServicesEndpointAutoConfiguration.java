package io.zhijun.dev.actuator.autoconfigure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.dev.actuator.DevServicesEndpoint;
import io.zhijun.dev.api.registration.LocalServiceRegistration;
import io.zhijun.dev.autoconfigure.bootstrap.ConditionalOnDevMode;
import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;

/**
 * Auto-configuration for the Dev Services Actuator endpoint.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@ConditionalOnDevMode
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
public class DevServicesEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DevServicesEndpoint devServicesEndpoint(ObjectProvider<LocalServiceRegistration> registrations) {
        Map<String, LocalServiceRegistration> registrationMap = new HashMap<String, LocalServiceRegistration>();
        for (LocalServiceRegistration registration : registrations) {
            registrationMap.put(registration.getName(), registration);
        }
        return new DevServicesEndpoint(registrationMap);
    }
}
