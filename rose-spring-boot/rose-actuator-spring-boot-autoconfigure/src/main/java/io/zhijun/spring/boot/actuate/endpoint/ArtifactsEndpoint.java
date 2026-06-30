package io.zhijun.spring.boot.actuate.endpoint;

import java.util.List;

import io.zhijun.spring.boot.classloading.Artifact;
import io.zhijun.spring.boot.classloading.ArtifactDetector;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "artifacts")
public class ArtifactsEndpoint {

    private final ArtifactDetector artifactDetector;

    public ArtifactsEndpoint(ClassLoader classLoader) {
        this.artifactDetector = new ArtifactDetector(classLoader);
    }

    @ReadOperation
    public List<Artifact> getArtifactMetaInfoList() {
        return artifactDetector.detect(false);
    }
}
