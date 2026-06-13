package io.zhijun.dev.services.core.autoconfigure;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleDevServicesFailureAnalyzerTests {

    private final MultipleDevServicesFailureAnalyzer analyzer = new MultipleDevServicesFailureAnalyzer();

    @Test
    void shouldProduceActionableFailureAnalysis() {
        List<String> services = Arrays.asList("lgtm", "openlit");
        MultipleDevServicesException exception = new MultipleDevServicesException("opentelemetry", services);

        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("opentelemetry", "lgtm", "openlit");
        assertThat(analysis.getAction()).contains("rose.dev.services", "lgtm");
    }
}
