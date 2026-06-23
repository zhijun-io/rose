package io.zhijun.dev.core.autoconfigure;

import io.zhijun.dev.core.autoconfigure.DevServiceProperties;
import io.zhijun.dev.core.autoconfigure.LocalServiceAutoConfiguration;
import io.zhijun.dev.core.autoconfigure.MultipleLocalServiceException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.dev.api.provider.LocalServiceProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link LocalServiceAutoConfiguration}.
 */
class LocalServiceAutoConfigurationTests {

    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LocalServiceAutoConfiguration.class));

    @Test
    void propertiesBeanIsAvailable() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(DevServiceProperties.class));
    }

    @Test
    void noConflictWithNoProviders() {
        contextRunner.run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void noConflictWithSingleProvider() {
        contextRunner
                .withBean("lgtm", LocalServiceProvider.class, () -> LocalServiceProvider.of("lgtm", "opentelemetry"))
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void noConflictWithProvidersInDifferentCategories() {
        contextRunner
                .withBean("lgtm", LocalServiceProvider.class, () -> LocalServiceProvider.of("lgtm", "opentelemetry"))
                .withBean("postgresql", LocalServiceProvider.class, () -> LocalServiceProvider.of("postgresql", "jdbc"))
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void conflictDetectedWithMultipleProvidersInSameCategory() {
        contextRunner
                .withBean("lgtm", LocalServiceProvider.class, () -> LocalServiceProvider.of("lgtm", "opentelemetry"))
                .withBean("openlit", LocalServiceProvider.class, () -> LocalServiceProvider.of("openlit", "opentelemetry"))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .isInstanceOf(MultipleLocalServiceException.class);
                });
    }

}

