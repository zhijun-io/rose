package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.micrometer.core.instrument.config.validate.Validated;
import io.micrometer.registry.otlp.OtlpConfig;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Implementation of {@link OtlpConfig} for OpenTelemetry unified annotation.
 */
class MicrometerOtlpConfig implements OtlpConfig {

    private final boolean enabled;
    private final String url;
    private final Duration step;
    private final Map<String, String> resourceAttributes;

    private MicrometerOtlpConfig(Builder builder) {
        Assert.hasText(builder.url, "url cannot be null or empty");

        this.enabled = builder.enabled;
        this.url = builder.url;
        this.step = builder.step;
        this.resourceAttributes = Collections.unmodifiableMap(new HashMap<>(builder.resourceAttributes));
    }

    /**
     * Creates a new builder for MicrometerOtlpConfig.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    @Nullable
    public String get(String key) {
        return null;
    }

    @Override
    public String prefix() {
        return MicrometerRegistryOtlpProperties.CONFIG_PREFIX;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public Duration step() {
        return step;
    }

    @Override
    public Map<String, String> resourceAttributes() {
        return resourceAttributes;
    }

    @Override
    public Validated<?> validate() {
        return Validated.none();
    }

    /**
     * Builder for MicrometerOtlpConfig.
     */
    static class Builder {
        private boolean enabled = true;
        private String url;
        private Duration step = Duration.ofSeconds(60);
        private final Map<String, String> resourceAttributes = new HashMap<>();

        private Builder() {}

        Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        Builder url(String url) {
            this.url = url;
            return this;
        }

        Builder step(Duration step) {
            Assert.notNull(step, "step cannot be null");
            this.step = step;
            return this;
        }

        Builder addResourceAttributes(Map<String, String> resourceAttributes) {
            Assert.notNull(resourceAttributes, "resourceAttributes cannot be null");
            Assert.noNullElements(resourceAttributes.keySet().toArray(), "resourceAttributes cannot contain null keys");
            this.resourceAttributes.putAll(new HashMap<>(resourceAttributes));
            return this;
        }

        MicrometerOtlpConfig build() {
            return new MicrometerOtlpConfig(this);
        }
    }
}
