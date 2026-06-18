package io.zhijun.local.core.autoconfigure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * A {@link org.springframework.boot.diagnostics.FailureAnalyzer} that provides actionable feedback
 * when multiple dev services in the same category are detected.
 */
public class MultipleLocalServiceFailureAnalyzer extends AbstractFailureAnalyzer<MultipleLocalServiceException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, MultipleLocalServiceException cause) {
        String firstService = cause.getServiceNames().isEmpty() ? "unknown" : cause.getServiceNames().get(0);
        return new FailureAnalysis(
                String.format(
                        "Multiple %s dev services detected: %s.",
                        cause.getCategory(),
                        cause.getServiceNames()),
                String.format(
                        "Disable all but one %s dev service by setting the enabled property to false "
                                + "(e.g., rose.local.%s.enabled=false).",
                        cause.getCategory(),
                        firstService),
                cause);
    }
}
