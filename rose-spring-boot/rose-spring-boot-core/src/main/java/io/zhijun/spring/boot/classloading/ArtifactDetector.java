package io.zhijun.spring.boot.classloading;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.zhijun.spring.boot.diagnostics.ClasspathMavenArtifactScanner;
import io.zhijun.spring.boot.diagnostics.ClasspathMavenArtifactScanner.MavenCoordinate;

public class ArtifactDetector {

    private final ClassLoader classLoader;

    public ArtifactDetector(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<Artifact> detect(boolean includeOptional) {
        ClasspathMavenArtifactScanner scanner = new ClasspathMavenArtifactScanner();
        try {
            List<MavenCoordinate> coordinates = scanner.scan(classLoader);
            List<Artifact> artifacts = new ArrayList<>(coordinates.size());
            for (MavenCoordinate coordinate : coordinates) {
                artifacts.add(new Artifact(
                        coordinate.getGroupId(),
                        coordinate.getArtifactId(),
                        coordinate.getVersion(),
                        coordinate.getLocation()));
            }
            return artifacts;
        }
        catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
