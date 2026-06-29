package io.zhijun.observation.boot.autoconfigure.otel.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.zhijun.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter;
import io.zhijun.spring.boot.bootstrap.config.DefaultConfigEnvironmentPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

/**
 * Verifies {@code config/default/opentelemetry.properties} exclusions load with Rose Spring Boot core.
 */
class OpenTelemetryDefaultConfigExcludeTests {

    private static final String OTLP_METRICS_EXPORT_AUTO_CONFIGURATION =
            "org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration";

    @Test
    void shouldLoadOtlpMetricsExportExclusionFromRoseDefaults() {
        MockEnvironment environment = new MockEnvironment();
        new DefaultConfigEnvironmentPostProcessor()
                .postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty(
                        ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME))
                .contains(OTLP_METRICS_EXPORT_AUTO_CONFIGURATION);
        assertThat(ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses(environment))
                .contains(OTLP_METRICS_EXPORT_AUTO_CONFIGURATION);
    }
}
