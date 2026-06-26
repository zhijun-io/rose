package io.zhijun.observation.boot.autoconfigure.otel.logs;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OpenTelemetry logs.
 */
@ConfigurationProperties(prefix = OpenTelemetryLoggingProperties.CONFIG_PREFIX)
public class OpenTelemetryLoggingProperties {

    public static final String CONFIG_PREFIX = "rose.otel.logs";

    public static final String ENABLED_PROPERTY = CONFIG_PREFIX + ".enabled";

    private final LogLimits limits = new LogLimits();

    private final LogRecordProcessorConfig processor = new LogRecordProcessorConfig();

    public LogLimits getLimits() {
        return limits;
    }

    public LogRecordProcessorConfig getProcessor() {
        return processor;
    }

    public static class LogLimits {

        private int maxAttributeValueLength = Integer.MAX_VALUE;

        private int maxNumberOfAttributes = 128;

        public int getMaxAttributeValueLength() {
            return maxAttributeValueLength;
        }

        public void setMaxAttributeValueLength(int maxAttributeValueLength) {
            this.maxAttributeValueLength = maxAttributeValueLength;
        }

        public int getMaxNumberOfAttributes() {
            return maxNumberOfAttributes;
        }

        public void setMaxNumberOfAttributes(int maxNumberOfAttributes) {
            this.maxNumberOfAttributes = maxNumberOfAttributes;
        }
    }

    public static class LogRecordProcessorConfig {

        private Duration scheduleDelay = Duration.ofSeconds(1);

        private Duration exportTimeout = Duration.ofSeconds(30);

        private int maxQueueSize = 2048;

        private int maxExportBatchSize = 512;

        private boolean metrics = false;

        public Duration getScheduleDelay() {
            return scheduleDelay;
        }

        public void setScheduleDelay(Duration scheduleDelay) {
            this.scheduleDelay = scheduleDelay;
        }

        public Duration getExportTimeout() {
            return exportTimeout;
        }

        public void setExportTimeout(Duration exporterTimeout) {
            this.exportTimeout = exporterTimeout;
        }

        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public void setMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
        }

        public int getMaxExportBatchSize() {
            return maxExportBatchSize;
        }

        public void setMaxExportBatchSize(int maxExportBatchSize) {
            this.maxExportBatchSize = maxExportBatchSize;
        }

        public boolean isMetrics() {
            return metrics;
        }

        public void setMetrics(boolean metrics) {
            this.metrics = metrics;
        }
    }
}
