package io.zhijun.devservice.core.autoconfigure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.devservice.api.provider.DevServiceProvider;
import io.zhijun.devservice.core.registration.DevServiceContainersInitializer;

/**
 * Global dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DevServiceProperties.class)
public final class DevServiceAutoConfiguration {

    @Bean
    static DevServiceContainersInitializer devServiceContainersInitializer() {
        return new DevServiceContainersInitializer();
    }

    @Bean
    SmartInitializingSingleton devServicesConflictValidator(ObjectProvider<DevServiceProvider> providers) {
        return () -> {
            Map<String, List<DevServiceProvider>> grouped = new java.util.HashMap<String, List<DevServiceProvider>>();
            for (DevServiceProvider provider : providers) {
                List<DevServiceProvider> group = grouped.get(provider.category());
                if (group == null) {
                    group = new ArrayList<>();
                    grouped.put(provider.category(), group);
                }
                group.add(provider);
            }
            for (Map.Entry<String, List<DevServiceProvider>> entry : grouped.entrySet()) {
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
