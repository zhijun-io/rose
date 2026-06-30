package io.zhijun.devservice.boot.autoconfigure;

import io.zhijun.devservice.boot.registration.DevServiceContainersInitializer;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DevServiceProperties.class)
public final class DevServiceAutoConfiguration {

    private final DevServiceConflictValidatorTemplate template = new DevServiceConflictValidatorTemplate();

    @Bean
    static DevServiceContainersInitializer devServiceContainersInitializer(BeanFactory beanFactory) {
        return new DevServiceContainersInitializer(beanFactory);
    }

    @Bean
    SmartInitializingSingleton devServicesConflictValidator(ObjectProvider<DevServiceProvider> providers) {
        return template.conflictValidator(providers);
    }
}
