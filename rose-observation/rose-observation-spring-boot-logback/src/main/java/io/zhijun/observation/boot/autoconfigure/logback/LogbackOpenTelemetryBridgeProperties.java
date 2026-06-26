package io.zhijun.observation.boot.autoconfigure.logback;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = LogbackOpenTelemetryBridgeProperties.CONFIG_PREFIX)
public class LogbackOpenTelemetryBridgeProperties {

    public static final String CONFIG_PREFIX = "rose.otel.logs.logback-bridge";

    /**
     * Whether to enable the Logback OpenTelemetry bridge.
     */
    private boolean enabled = true;

    /**
     * Enable the capture of Logback log event arguments.
     */
    private boolean captureArguments = false;

    /**
     * Enable the capture of the Logback log event message template (if arguments are provided).
     */
    private boolean captureTemplate = false;

    /**
     * Enable the capture of source code attributes. Note that capturing source code attributes at
     * logging sites might add a performance overhead.
     */
    private boolean captureCodeAttributes = false;

    /**
     * Enable the capture of experimental log attributes 'thread.name' and 'thread.id'.
     */
    private boolean captureExperimentalAttributes = false;

    /**
     * Enable the capture of Logback key value pairs as attributes.
     */
    private boolean captureKeyValuePairAttributes = false;

    /**
     * Enable the capture of Logback logger context properties as attributes.
     */
    private boolean captureLoggerContext = false;

    /**
     * Enable the capture of Logstash marker attributes, added to logs via 'Markers.append()',
     * 'Markers.appendEntries()', 'Markers.appendArray()' and 'Markers.appendRaw()' methods.
     */
    private boolean captureLogstashMarkerAttributes = false;

    /**
     * Enable the capture of Logback markers as attributes.
     */
    private boolean captureMarkerAttribute = false;

    /**
     * Comma separated list of MDC attributes to capture. Use the wildcard character '*' to capture all attributes.
     */
    private String captureMdcAttributes = "";

    /**
     * Log telemetry is emitted after the initialization of the OpenTelemetry Logback appender with an OpenTelemetry
     * object. This setting allows you to modify the size of the cache used to replay the first logs. 'thread.id'
     * attribute is not captured.
     */
    private int numLogsCapturedBeforeOtelInstall = 1000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isCaptureArguments() {
        return captureArguments;
    }

    public void setCaptureArguments(boolean captureArguments) {
        this.captureArguments = captureArguments;
    }

    public boolean isCaptureTemplate() {
        return captureTemplate;
    }

    public void setCaptureTemplate(boolean captureTemplate) {
        this.captureTemplate = captureTemplate;
    }

    public boolean isCaptureCodeAttributes() {
        return captureCodeAttributes;
    }

    public void setCaptureCodeAttributes(boolean captureCodeAttributes) {
        this.captureCodeAttributes = captureCodeAttributes;
    }

    public boolean isCaptureExperimentalAttributes() {
        return captureExperimentalAttributes;
    }

    public void setCaptureExperimentalAttributes(boolean captureExperimentalAttributes) {
        this.captureExperimentalAttributes = captureExperimentalAttributes;
    }

    public boolean isCaptureKeyValuePairAttributes() {
        return captureKeyValuePairAttributes;
    }

    public void setCaptureKeyValuePairAttributes(boolean captureKeyValuePairAttributes) {
        this.captureKeyValuePairAttributes = captureKeyValuePairAttributes;
    }

    public boolean isCaptureLoggerContext() {
        return captureLoggerContext;
    }

    public void setCaptureLoggerContext(boolean captureLoggerContext) {
        this.captureLoggerContext = captureLoggerContext;
    }

    public boolean isCaptureLogstashMarkerAttributes() {
        return captureLogstashMarkerAttributes;
    }

    public void setCaptureLogstashMarkerAttributes(boolean captureLogstashMarkerAttributes) {
        this.captureLogstashMarkerAttributes = captureLogstashMarkerAttributes;
    }

    public boolean isCaptureMarkerAttribute() {
        return captureMarkerAttribute;
    }

    public void setCaptureMarkerAttribute(boolean captureMarkerAttribute) {
        this.captureMarkerAttribute = captureMarkerAttribute;
    }

    public String getCaptureMdcAttributes() {
        return captureMdcAttributes;
    }

    public void setCaptureMdcAttributes(String captureMdcAttributes) {
        this.captureMdcAttributes = captureMdcAttributes;
    }

    public int getNumLogsCapturedBeforeOtelInstall() {
        return numLogsCapturedBeforeOtelInstall;
    }

    public void setNumLogsCapturedBeforeOtelInstall(int numLogsCapturedBeforeOtelInstall) {
        this.numLogsCapturedBeforeOtelInstall = numLogsCapturedBeforeOtelInstall;
    }
}
