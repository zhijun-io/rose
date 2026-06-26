package io.zhijun.devservice.boot.autoconfigure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.devservice.boot.registration.DevServiceContainersInitializer;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;

/**
 * Global dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DevServiceProperties.class)
public final class DevServiceAutoConfiguration {

    @Bean
    static DevServiceContainersInitializer devServiceContainersInitializer(BeanFactory beanFactory) {
        return new DevServiceContainersInitializer(beanFactory);
    }

    @Bean
    SmartInitializingSingleton devServicesConflictValidator(ObjectProvider<DevServiceProvider> providers) {
        return () -> {
            Map<DevServiceCategory, List<DevServiceProvider>> grouped =
                    new EnumMap<DevServiceCategory, List<DevServiceProvider>>(DevServiceCategory.class);
            for (DevServiceProvider provider : providers) {
                List<DevServiceProvider> group = grouped.get(provider.category());
                if (group == null) {
                    group = new ArrayList<DevServiceProvider>();
                    grouped.put(provider.category(), group);
                }
                group.add(provider);
            }
            for (Map.Entry<DevServiceCategory, List<DevServiceProvider>> entry : grouped.entrySet()) {
                if (entry.getValue().size() > 1) {
                    List<String> names = new ArrayList<String>();
                    for (DevServiceProvider provider : entry.getValue()) {
                        names.add(provider.name());
                    }
                    Collections.sort(names);
                    throw new MultipleDevServiceException(entry.getKey(), names);
                }
            }
        };
    }
}
