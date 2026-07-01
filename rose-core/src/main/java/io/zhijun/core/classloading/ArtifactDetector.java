package io.zhijun.core.classloading;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArtifactDetector {

    private final ClassLoader classLoader;

    public ArtifactDetector(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<Artifact> detect() {
        ClasspathMavenArtifactScanner scanner = new ClasspathMavenArtifactScanner();
        try {
            return scanner.scan(classLoader);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
