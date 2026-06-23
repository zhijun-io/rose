package io.zhijun.dev.core.autoconfigure;

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

import io.zhijun.dev.api.provider.LocalServiceProvider;
import io.zhijun.dev.core.registration.LocalServiceContainersInitializer;

/**
 * Global dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DevServiceProperties.class)
public final class LocalServiceAutoConfiguration {

    @Bean
    static LocalServiceContainersInitializer devServiceContainersInitializer() {
        return new LocalServiceContainersInitializer();
    }

    @Bean
    SmartInitializingSingleton devServicesConflictValidator(ObjectProvider<LocalServiceProvider> providers) {
        return new SmartInitializingSingleton() {
            @Override
            public void afterSingletonsInstantiated() {
                Map<String, List<LocalServiceProvider>> grouped = new java.util.HashMap<String, List<LocalServiceProvider>>();
                for (LocalServiceProvider provider : providers) {
                    List<LocalServiceProvider> group = grouped.get(provider.category());
                    if (group == null) {
                        group = new ArrayList<LocalServiceProvider>();
                        grouped.put(provider.category(), group);
                    }
                    group.add(provider);
                }
                for (Map.Entry<String, List<LocalServiceProvider>> entry : grouped.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        List<String> names = new ArrayList<String>();
                        for (LocalServiceProvider provider : entry.getValue()) {
                            names.add(provider.name());
                        }
                        Collections.sort(names);
                        throw new MultipleLocalServiceException(entry.getKey(), names);
                    }
                }
            }
        };
    }
}
