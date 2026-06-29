package io.zhijun.observation.boot.autoconfigure;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.zhijun.observation.boot.autoconfigure.conventions.TelemetryConventionsBackend;

/**
 * Selects a single {@link TelemetryConventionsBackend} from registered candidates.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ObservationProperties.class)
@ConditionalOnProperty(prefix = ObservationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
@ConditionalOnBean(TelemetryConventionsBackend.class)
public class ConventionsSelectorAutoConfiguration {

    @Bean(name = "primaryTelemetryConventionsBackend")
    @Primary
    @ConditionalOnMissingBean(name = "primaryTelemetryConventionsBackend")
    TelemetryConventionsBackend primaryTelemetryConventionsBackend(
            ObservationProperties properties, ObjectProvider<TelemetryConventionsBackend> backends) {
        List<TelemetryConventionsBackend> candidates = backends.orderedStream().collect(Collectors.toList());
        String configured = properties.getConventions().getBackend();
        if (configured != null && !configured.trim().isEmpty()) {
            String backendId = configured.trim();
            return candidates.stream()
                    .filter(backend -> backend.id().equals(backendId))
                    .findFirst()
                    .orElseThrow(() -> new UnknownConventionsBackendException(backendId, candidates));
        }

        List<TelemetryConventionsBackend> defaults = candidates.stream()
                .filter(TelemetryConventionsBackend::defaultCandidate)
                .collect(Collectors.toList());
        if (defaults.size() == 1) {
            return defaults.get(0);
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        throw new AmbiguousConventionsBackendException(candidates);
    }
}
