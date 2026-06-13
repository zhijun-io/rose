package io.zhijun.observation.autoconfigure;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.conventions.AiObservationConventionsProvider;

/**
 * Auto-configuration for observations.
 */
@Configuration(proxyBeanMethods = false)
public class ObservationAutoConfiguration {

    @Bean
    SmartInitializingSingleton observationConventionsValidator(
            ObjectProvider<AiObservationConventionsProvider> providers) {
        return () -> {
            List<String> names = providers.orderedStream()
                    .map(AiObservationConventionsProvider::name)
                    .sorted()
                    .collect(Collectors.toList());
            if (names.size() > 1) {
                throw new MultipleAiObservationConventionsException(names);
            }
        };
    }
}
