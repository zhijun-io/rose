package io.zhijun.observation.opentelemetry.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.observation.conventions.AiObservationConventionsProvider;
import io.zhijun.opentelemetry.autoconfigure.resource.OpenTelemetryResourceBuilderCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link OpenTelemetryConventionsAutoConfiguration}.
 */
class OpenTelemetryConventionsAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryConventionsAutoConfiguration.class));

    @Test
    void registersAiObservationConventionsProvider() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AiObservationConventionsProvider.class);
            assertThat(context.getBean(AiObservationConventionsProvider.class).name()).isEqualTo("opentelemetry");
        });
    }

    @Test
    void registersResourceBuilderCustomizerWhenOnClasspath() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(OpenTelemetryResourceBuilderCustomizer.class));
    }

    @Test
    void noResourceBuilderCustomizerWhenNotOnClasspath() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(OpenTelemetryResourceBuilderCustomizer.class))
                .run(context ->
                        assertThat(context).doesNotHaveBean(OpenTelemetryResourceBuilderCustomizer.class));
    }

}
