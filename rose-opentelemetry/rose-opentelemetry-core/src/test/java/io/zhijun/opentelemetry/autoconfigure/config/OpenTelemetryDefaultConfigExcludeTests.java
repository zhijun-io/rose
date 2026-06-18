package io.zhijun.opentelemetry.autoconfigure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import io.zhijun.boot.autoconfigure.RoseAutoConfigurationImportFilter;
import io.zhijun.boot.env.defaults.DefaultConfigPropertiesEnvironmentPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies {@code config/default/opentelemetry.properties} exclusions load with Rose Spring Boot core.
 */
class OpenTelemetryDefaultConfigExcludeTests {

    private static final String OTLP_METRICS_EXPORT_AUTO_CONFIGURATION =
            "org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration";

    @Test
    void shouldLoadOtlpMetricsExportExclusionFromRoseDefaults() {
        MockEnvironment environment = new MockEnvironment();
        new DefaultConfigPropertiesEnvironmentPostProcessor()
                .postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty(RoseAutoConfigurationImportFilter.EXCLUDE_PROPERTY))
                .contains(OTLP_METRICS_EXPORT_AUTO_CONFIGURATION);
        assertThat(RoseAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses(environment))
                .contains(OTLP_METRICS_EXPORT_AUTO_CONFIGURATION);
    }
}
