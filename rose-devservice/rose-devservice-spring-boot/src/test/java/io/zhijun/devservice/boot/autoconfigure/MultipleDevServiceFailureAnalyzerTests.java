package io.zhijun.devservice.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import io.zhijun.devservice.core.api.provider.DevServiceCategory;

class MultipleDevServiceFailureAnalyzerTests {

    private final MultipleDevServiceFailureAnalyzer analyzer = new MultipleDevServiceFailureAnalyzer();

    @Test
    void shouldProduceActionableFailureAnalysis() {
        List<String> services = Arrays.asList("lgtm", "openlit");
        MultipleDevServiceException exception =
                new MultipleDevServiceException(DevServiceCategory.OPENTELEMETRY, services);

        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("opentelemetry", "lgtm", "openlit");
        assertThat(analysis.getAction()).contains("rose.dev", "lgtm");
    }
}
