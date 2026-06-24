package io.zhijun.observability.core.autoconfigure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Actionable startup failure when multiple conventions backends are present.
 */
public class AmbiguousConventionsBackendFailureAnalyzer
        extends AbstractFailureAnalyzer<AmbiguousConventionsBackendException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, AmbiguousConventionsBackendException cause) {
        return new FailureAnalysis(
                String.format("Multiple telemetry conventions backends detected: %s.", cause.getBackendIds()),
                String.format(
                        "Set rose.observability.conventions.backend to one of %s, "
                                + "or remove unused conventions modules from the classpath.",
                        cause.getBackendIds()),
                cause);
    }
}
