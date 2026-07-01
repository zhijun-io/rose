package io.zhijun.spring.boot.actuator.endpoint;

import io.zhijun.core.classloading.Artifact;
import io.zhijun.core.classloading.ArtifactDetector;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

@Endpoint(id = "artifacts")
public class ArtifactsEndpoint {

    private final ArtifactDetector artifactDetector;

    public ArtifactsEndpoint(ClassLoader classLoader) {
        this.artifactDetector = new ArtifactDetector(classLoader);
    }

    @ReadOperation
    public List<Artifact> getArtifactMetaInfoList() {
        return artifactDetector.detect();
    }
}
