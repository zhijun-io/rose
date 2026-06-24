package io.zhijun.observation.boot.autoconfigure.micrometer.otlp;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link MicrometerOtlpConfig}.
 */
class MicrometerOtlpConfigTests {

    @Test
    void shouldHaveCorrectConfigPrefix() {
        MicrometerOtlpConfig config = MicrometerOtlpConfig.builder()
                .url("http://localhost:4318/v1/metrics")
                .build();

        assertThat(config.prefix()).isEqualTo(MicrometerRegistryOtlpProperties.CONFIG_PREFIX);
    }

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        String url = "http://localhost:4318/v1/metrics";
        MicrometerOtlpConfig config = MicrometerOtlpConfig.builder()
                .url(url)
                .build();

        assertThat(config.enabled()).isTrue();
        assertThat(config.url()).isEqualTo(url);
        assertThat(config.step()).isEqualTo(Duration.ofSeconds(60));
        assertThat(config.resourceAttributes()).isEmpty();
    }

    @Test
    void shouldUpdateValues() {
        Map<String, String> resourceAttributes = new HashMap<String, String>();
        resourceAttributes.put("service.name", "test-service");
        resourceAttributes.put("service.version", "1.0.0");

        MicrometerOtlpConfig config = MicrometerOtlpConfig.builder()
                .url("http://example.com:4318/v1/metrics")
                .enabled(false)
                .step(Duration.ofSeconds(30))
                .addResourceAttributes(resourceAttributes)
                .build();

        assertThat(config.enabled()).isFalse();
        assertThat(config.url()).isEqualTo("http://example.com:4318/v1/metrics");
        assertThat(config.step()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.resourceAttributes()).containsAllEntriesOf(resourceAttributes);
    }

    @Test
    void shouldReturnNullForGet() {
        MicrometerOtlpConfig config = MicrometerOtlpConfig.builder()
                .url("http://localhost:4318/v1/metrics")
                .build();

        assertThat(config.get("any-key")).isNull();
    }

    @Test
    void shouldValidateSuccessfully() {
        MicrometerOtlpConfig config = MicrometerOtlpConfig.builder()
                .url("http://localhost:4318/v1/metrics")
                .build();

        assertThat(config.validate().isValid()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUrlIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MicrometerOtlpConfig.builder().url(null).build())
                .withMessage("url cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenUrlIsEmpty() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MicrometerOtlpConfig.builder().url("").build())
                .withMessage("url cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenStepIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MicrometerOtlpConfig.builder().step(null))
                .withMessage("step cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenResourceAttributesIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MicrometerOtlpConfig.builder().addResourceAttributes(null))
                .withMessage("resourceAttributes cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenResourceAttributesHasNullKey() {
        HashMap resourceAttributes = new HashMap<String, String>();
        resourceAttributes.put(null, "value");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MicrometerOtlpConfig.builder().addResourceAttributes(resourceAttributes))
                .withMessage("resourceAttributes cannot contain null keys");
    }

    @Test
    void shouldReturnImmutableResourceAttributes() {
        Map<String, String> resourceAttributes = new HashMap<String, String>();
        resourceAttributes.put("service.name", "test");

        MicrometerOtlpConfig config = MicrometerOtlpConfig.builder()
                .url("http://localhost:4318/v1/metrics")
                .addResourceAttributes(resourceAttributes)
                .build();

        assertThatThrownBy(() -> config.resourceAttributes().put("extra", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

}
