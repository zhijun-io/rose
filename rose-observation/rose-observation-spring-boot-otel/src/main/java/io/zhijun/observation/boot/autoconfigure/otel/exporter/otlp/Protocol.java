package io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp;

/**
 * Transport protocol to use for OTLP requests.
 */
public enum Protocol {
    GRPC("grpc"),

    HTTP_PROTOBUF("http_protobuf");

    private final String configValue;

    Protocol(String configValue) {
        this.configValue = configValue;
    }

    public String configValue() {
        return configValue;
    }
}
