package io.zhijun.devservice.core.autoconfigure;

import java.util.Arrays;
import java.util.List;

import io.zhijun.devservice.core.autoconfigure.MultipleDevServiceException;
import io.zhijun.devservice.core.autoconfigure.MultipleDevServiceFailureAnalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleDevServiceFailureAnalyzerTests {

    private final MultipleDevServiceFailureAnalyzer analyzer = new MultipleDevServiceFailureAnalyzer();

    @Test
    void shouldProduceActionableFailureAnalysis() {
        List<String> services = Arrays.asList("lgtm", "openlit");
        MultipleDevServiceException exception = new MultipleDevServiceException("opentelemetry", services);

        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("opentelemetry", "lgtm", "openlit");
        assertThat(analysis.getAction()).contains("rose.dev", "lgtm");
    }
}
