package io.zhijun.devservice.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.zhijun.devservice.core.api.provider.DevServiceCategory;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;

/**
 * Unit test for {@link DevServiceAutoConfiguration}.
 */
class DevServiceAutoConfigurationTests {

    private static final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(DevServiceAutoConfiguration.class));

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
                .withBean(
                        "lgtm",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("lgtm", DevServiceCategory.OLLAMA))
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void noConflictWithProvidersInDifferentCategories() {
        contextRunner
                .withBean(
                        "lgtm",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("lgtm", DevServiceCategory.OLLAMA))
                .withBean(
                        "postgresql",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("postgresql", DevServiceCategory.JDBC))
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void conflictDetectedWithMultipleProvidersInSameCategory() {
        contextRunner
                .withBean(
                        "lgtm",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("lgtm", DevServiceCategory.OLLAMA))
                .withBean(
                        "openlit",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("openlit", DevServiceCategory.OLLAMA))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isInstanceOf(MultipleDevServiceException.class);
                });
    }

    @Test
    void conflictDetectedWithMultipleJdbcProviders() {
        contextRunner
                .withBean(
                        "postgresql",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("postgresql", DevServiceCategory.JDBC))
                .withBean(
                        "mysql",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("mysql", DevServiceCategory.JDBC))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isInstanceOf(MultipleDevServiceException.class);
                });
    }

    @Test
    void conflictDetectedWithMultipleJmsProviders() {
        contextRunner
                .withBean(
                        "artemis",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("artemis", DevServiceCategory.JMS))
                .withBean(
                        "activemq",
                        DevServiceProvider.class,
                        () -> DevServiceProvider.of("activemq", DevServiceCategory.JMS))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isInstanceOf(MultipleDevServiceException.class);
                });
    }
}
