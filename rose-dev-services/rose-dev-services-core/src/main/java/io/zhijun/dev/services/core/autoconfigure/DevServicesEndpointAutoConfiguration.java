package io.zhijun.dev.services.core.autoconfigure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.boot.autoconfigure.bootstrap.ConditionalOnDevMode;
import io.zhijun.dev.services.api.registration.DevServiceRegistration;
import io.zhijun.dev.services.core.actuate.endpoint.DevServicesEndpoint;

/**
 * Auto-configuration for the Dev Services Actuator endpoint.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@ConditionalOnDevMode
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
public class DevServicesEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DevServicesEndpoint devServicesEndpoint(ObjectProvider<DevServiceRegistration> registrations) {
        Map<String, DevServiceRegistration> registrationMap = new HashMap<String, DevServiceRegistration>();
        for (DevServiceRegistration registration : registrations) {
            registrationMap.put(registration.getName(), registration);
        }
        return new DevServicesEndpoint(registrationMap);
    }
}
