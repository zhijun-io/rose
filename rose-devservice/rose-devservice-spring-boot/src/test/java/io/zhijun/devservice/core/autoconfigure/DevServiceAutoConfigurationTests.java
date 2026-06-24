package io.zhijun.devservice.core.autoconfigure;

import io.zhijun.devservice.core.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.core.autoconfigure.MultipleDevServiceException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.devservice.api.provider.DevServiceProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DevServiceAutoConfiguration}.
 */
class DevServiceAutoConfigurationTests {

    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DevServiceAutoConfiguration.class));

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
                .withBean("lgtm", DevServiceProvider.class, () -> DevServiceProvider.of("lgtm", "opentelemetry"))
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void noConflictWithProvidersInDifferentCategories() {
        contextRunner
                .withBean("lgtm", DevServiceProvider.class, () -> DevServiceProvider.of("lgtm", "opentelemetry"))
                .withBean("postgresql", DevServiceProvider.class, () -> DevServiceProvider.of("postgresql", "jdbc"))
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void conflictDetectedWithMultipleProvidersInSameCategory() {
        contextRunner
                .withBean("lgtm", DevServiceProvider.class, () -> DevServiceProvider.of("lgtm", "opentelemetry"))
                .withBean("openlit", DevServiceProvider.class, () -> DevServiceProvider.of("openlit", "opentelemetry"))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .isInstanceOf(MultipleDevServiceException.class);
                });
    }

    @Test
    void conflictDetectedWithMultipleJdbcProviders() {
        contextRunner
                .withBean("postgresql", DevServiceProvider.class, () -> DevServiceProvider.of("postgresql", "jdbc"))
                .withBean("mysql", DevServiceProvider.class, () -> DevServiceProvider.of("mysql", "jdbc"))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .isInstanceOf(MultipleDevServiceException.class);
                });
    }

    @Test
    void conflictDetectedWithMultipleJmsProviders() {
        contextRunner
                .withBean("artemis", DevServiceProvider.class, () -> DevServiceProvider.of("artemis", "jms"))
                .withBean("activemq", DevServiceProvider.class, () -> DevServiceProvider.of("activemq", "jms"))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .isInstanceOf(MultipleDevServiceException.class);
                });
    }

}

