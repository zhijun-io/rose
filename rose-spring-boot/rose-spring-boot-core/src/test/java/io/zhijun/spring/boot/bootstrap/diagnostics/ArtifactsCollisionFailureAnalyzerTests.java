package io.zhijun.spring.boot.bootstrap.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

class ArtifactsCollisionFailureAnalyzerTests {

    private final ArtifactsCollisionFailureAnalyzer analyzer = new ArtifactsCollisionFailureAnalyzer();

    @Test
    void shouldAnalyzeArtifactsCollisionException() {
        ArtifactsCollisionException exception =
                new ArtifactsCollisionException("collision", Collections.singleton("com.example:demo"));

        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        assertThat(analysis.getDescription()).contains("collision");
        assertThat(analysis.getAction()).contains("mvn dependency:tree");
        assertThat(analysis.getAction()).contains("com.example:demo");
    }
}
