package io.zhijun.observation.boot.autoconfigure.otel.common;

/**
 * String values used by exporter conditions and annotations.
 */
public final class ExporterTypeNames {

    public static final String CONSOLE = "console";

    public static final String OTLP = "otlp";

    public static final String NONE = "none";

    public static final String DEFAULT = OTLP;

    private ExporterTypeNames() {}
}
