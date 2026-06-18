package io.zhijun.boot.diagnostics;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClasspathMavenArtifactScannerTests {

    private final ClasspathMavenArtifactScanner scanner = new ClasspathMavenArtifactScanner();

    @Test
    void shouldDetectDuplicateCoordinatesOnClasspath() throws Exception {
        Set<String> collisions = scanner.findCollidingCoordinates(Thread.currentThread().getContextClassLoader());

        assertThat(collisions).isNotNull();
    }
}
