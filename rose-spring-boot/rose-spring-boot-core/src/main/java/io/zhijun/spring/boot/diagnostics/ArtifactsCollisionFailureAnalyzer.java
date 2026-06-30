package io.zhijun.spring.boot.diagnostics;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import io.zhijun.core.annotation.Internal;

/**
 * Internal failure analyzer for {@link ArtifactsCollisionException}.
 */
@Internal
public final class ArtifactsCollisionFailureAnalyzer extends AbstractFailureAnalyzer<ArtifactsCollisionException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, ArtifactsCollisionException cause) {
        return new FailureAnalysis(cause.getMessage(), buildAction(cause), cause);
    }

    private static String buildAction(ArtifactsCollisionException cause) {
        StringBuilder action =
                new StringBuilder("Analyze conflicting artifacts with Maven dependency tree, for example:");
        action.append(System.lineSeparator());
        action.append("mvn dependency:tree -Dincludes=");
        boolean first = true;
        for (String artifact : cause.getArtifacts()) {
            if (!first) {
                action.append(',');
            }
            action.append(artifact);
            first = false;
        }
        action.append(System.lineSeparator());
        action.append("Then exclude redundant dependencies in pom.xml, or disable diagnosis with ");
        action.append(ArtifactsCollisionDiagnosisListener.ENABLED_PROPERTY).append("=false");
        return action.toString();
    }
}
