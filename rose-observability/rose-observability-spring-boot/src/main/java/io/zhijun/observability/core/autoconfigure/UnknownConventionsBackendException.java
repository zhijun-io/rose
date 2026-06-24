package io.zhijun.observability.core.autoconfigure;

import java.util.List;
import java.util.stream.Collectors;

import io.zhijun.observability.core.TelemetryConventionsBackend;

/**
 * Thrown when {@code rose.observability.conventions.backend} does not match any registered backend.
 */
public class UnknownConventionsBackendException extends RuntimeException {

    private final String configuredBackend;
    private final List<String> availableBackendIds;

    public UnknownConventionsBackendException(String configuredBackend, List<TelemetryConventionsBackend> available) {
        super("Unknown telemetry conventions backend '" + configuredBackend + "'. Available: "
                + available.stream().map(TelemetryConventionsBackend::id).sorted().collect(Collectors.toList()));
        this.configuredBackend = configuredBackend;
        this.availableBackendIds =
                available.stream().map(TelemetryConventionsBackend::id).sorted().collect(Collectors.toList());
    }

    public String getConfiguredBackend() {
        return configuredBackend;
    }

    public List<String> getAvailableBackendIds() {
        return availableBackendIds;
    }
}
