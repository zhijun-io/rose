package io.zhijun.observation.boot.autoconfigure;

import java.util.List;
import java.util.stream.Collectors;

import io.zhijun.observation.boot.autoconfigure.conventions.TelemetryConventionsBackend;

/**
 * Thrown when multiple conventions backends are present and none is explicitly selected.
 */
public class AmbiguousConventionsBackendException extends RuntimeException {

    private final List<String> backendIds;

    public AmbiguousConventionsBackendException(List<TelemetryConventionsBackend> backends) {
        super("Multiple telemetry conventions backends detected: "
                + backends.stream().map(TelemetryConventionsBackend::id).sorted().collect(Collectors.toList()));
        this.backendIds = backends.stream().map(TelemetryConventionsBackend::id).sorted().collect(Collectors.toList());
    }

    public List<String> getBackendIds() {
        return backendIds;
    }
}
