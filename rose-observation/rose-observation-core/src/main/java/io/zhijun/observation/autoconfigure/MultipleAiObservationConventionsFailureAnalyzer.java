package io.zhijun.observation.autoconfigure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.boot.diagnostics.FailureAnalyzer;

/**
 * A {@link FailureAnalyzer} that provides actionable feedback
 * when multiple AI observation convention modules are detected.
 */
public class MultipleAiObservationConventionsFailureAnalyzer
        extends AbstractFailureAnalyzer<MultipleAiObservationConventionsException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, MultipleAiObservationConventionsException cause) {
        return new FailureAnalysis(
                String.format("Multiple AI observation convention modules detected: %s.", cause.getConventionNames()),
                String.format(
                        "Disable all but one AI observation convention module by setting its enabled property to false "
                                + "(e.g., rose.observations.conventions.%s.ai.enabled=false).",
                        cause.getConventionNames().get(0)),
                cause);
    }
}
