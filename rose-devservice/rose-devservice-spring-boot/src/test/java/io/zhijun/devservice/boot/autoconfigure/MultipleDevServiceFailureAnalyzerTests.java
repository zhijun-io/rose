package io.zhijun.devservice.boot.autoconfigure;

import io.zhijun.devservice.core.api.provider.DevServiceCategory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleDevServiceFailureAnalyzerTests {

    private final MultipleDevServiceFailureAnalyzer analyzer = new MultipleDevServiceFailureAnalyzer();

    @Test
    void shouldProduceActionableFailureAnalysis() {
        List<String> services = Arrays.asList("lgtm", "openlit");
        MultipleDevServiceException exception =
                new MultipleDevServiceException(DevServiceCategory.OLLAMA, services);

        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("ollama", "lgtm", "openlit");
        assertThat(analysis.getAction()).contains("rose.dev", "lgtm");
    }
}
