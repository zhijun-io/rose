package io.zhijun.observation.boot.autoconfigure;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import io.zhijun.observation.core.TelemetryConventionsBackend;

import static org.assertj.core.api.Assertions.assertThat;

class AmbiguousConventionsBackendFailureAnalyzerTests {

    private final AmbiguousConventionsBackendFailureAnalyzer analyzer =
            new AmbiguousConventionsBackendFailureAnalyzer();

    @Test
    void analyzesAmbiguousBackends() {
        AmbiguousConventionsBackendException exception = new AmbiguousConventionsBackendException(Arrays.asList(
                TelemetryConventionsBackend.of("openinference"),
                TelemetryConventionsBackend.of("opentelemetry")));

        FailureAnalysis analysis = analyzer.analyze(exception);

        assertThat(analysis.getDescription()).contains("openinference", "opentelemetry");
        assertThat(analysis.getAction()).contains("rose.observation.conventions.backend");
    }
}
