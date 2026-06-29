package io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import io.opentelemetry.sdk.common.export.RetryPolicy;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link RetryConfig}.
 */
class RetryConfigTests {

    @Test
    void shouldHaveDefaultValues() {
        RetryConfig config = new RetryConfig();

        assertThat(config.getMaxAttempts()).isEqualTo(5);
        assertThat(config.getBackoffConfig().getFirstBackoff()).isEqualTo(Duration.ofSeconds(1));
        assertThat(config.getBackoffConfig().getMaxBackoff()).isEqualTo(Duration.ofSeconds(5));
        assertThat(config.getBackoffConfig().getMultiplier()).isEqualTo(1.5);
    }

    @Test
    void shouldUpdateValues() {
        RetryConfig config = new RetryConfig();
        config.setMaxAttempts(3);
        config.getBackoffConfig().setFirstBackoff(Duration.ofMillis(500));
        config.getBackoffConfig().setMaxBackoff(Duration.ofSeconds(2));
        config.getBackoffConfig().setMultiplier(2.0);

        assertThat(config.getMaxAttempts()).isEqualTo(3);
        assertThat(config.getBackoffConfig().getFirstBackoff()).isEqualTo(Duration.ofMillis(500));
        assertThat(config.getBackoffConfig().getMaxBackoff()).isEqualTo(Duration.ofSeconds(2));
        assertThat(config.getBackoffConfig().getMultiplier()).isEqualTo(2.0);
    }

    @Test
    void shouldBuildRetryPolicy() {
        RetryConfig config = new RetryConfig();
        config.setMaxAttempts(4);
        config.getBackoffConfig().setFirstBackoff(Duration.ofMillis(250));
        config.getBackoffConfig().setMaxBackoff(Duration.ofSeconds(3));
        config.getBackoffConfig().setMultiplier(2.5);

        RetryPolicy policy = RetryConfig.buildRetryPolicy(config);

        assertThat(policy.getMaxAttempts()).isEqualTo(4);
        assertThat(policy.getInitialBackoff()).isEqualTo(Duration.ofMillis(250));
        assertThat(policy.getMaxBackoff()).isEqualTo(Duration.ofSeconds(3));
        assertThat(policy.getBackoffMultiplier()).isEqualTo(2.5);
    }
}
