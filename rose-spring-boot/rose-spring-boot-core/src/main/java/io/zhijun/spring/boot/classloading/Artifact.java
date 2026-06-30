package io.zhijun.spring.boot.classloading;

public class Artifact {

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String location;

    public Artifact(String groupId, String artifactId, String version, String location) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.location = location;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + " (" + location + ")";
    }
}
