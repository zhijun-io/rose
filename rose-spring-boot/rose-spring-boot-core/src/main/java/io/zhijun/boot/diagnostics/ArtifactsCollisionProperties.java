package io.zhijun.boot.diagnostics;

/**
 * Configuration keys for classpath artifact collision diagnosis.
 */
public final class ArtifactsCollisionProperties {

    public static final String CONFIG_PREFIX = "rose.diagnostics.artifacts-collision";

    public static final String ENABLED = CONFIG_PREFIX + ".enabled";

    private ArtifactsCollisionProperties() {
    }
}
