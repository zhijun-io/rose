package io.zhijun.local.core.autoconfigure;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleLocalServiceFailureAnalyzerTests {

    private final MultipleLocalServiceFailureAnalyzer analyzer = new MultipleLocalServiceFailureAnalyzer();

    @Test
    void shouldProduceActionableFailureAnalysis() {
        List<String> services = Arrays.asList("lgtm", "openlit");
        MultipleLocalServiceException exception = new MultipleLocalServiceException("opentelemetry", services);

        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("opentelemetry", "lgtm", "openlit");
        assertThat(analysis.getAction()).contains("rose.local", "lgtm");
    }
}
