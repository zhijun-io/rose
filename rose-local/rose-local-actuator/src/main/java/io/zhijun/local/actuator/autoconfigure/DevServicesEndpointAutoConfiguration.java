package io.zhijun.local.actuator.autoconfigure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.local.actuator.DevServicesEndpoint;
import io.zhijun.local.api.registration.LocalServiceRegistration;
import io.zhijun.local.autoconfigure.bootstrap.ConditionalOnDevMode;
import io.zhijun.local.core.autoconfigure.LocalServiceAutoConfiguration;

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
