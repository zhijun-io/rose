package io.zhijun.spring.boot.bootstrap.diagnostics;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import io.zhijun.core.annotation.Internal;

/**
 * Internal exception thrown when duplicate Maven coordinates are detected on the classpath.
 */
@Internal
public final class ArtifactsCollisionException extends RuntimeException {

    private final Set<String> artifacts;

    public ArtifactsCollisionException(String message, Set<String> artifacts) {
        super(message);
        this.artifacts = Collections.unmodifiableSet(new LinkedHashSet<String>(artifacts));
    }

    public Set<String> getArtifacts() {
        return artifacts;
    }
}
