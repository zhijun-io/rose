package io.zhijun.boot.autoconfigure.bootstrap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import io.zhijun.boot.bootstrap.BootstrapMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link BootstrapEnvironmentPostProcessor}.
 */
@TestMethodOrder(MethodOrderer.Random.class)
class BootstrapEnvironmentPostProcessorTests {

    private final BootstrapEnvironmentPostProcessor processor = new BootstrapEnvironmentPostProcessor();

    @BeforeEach
    void setUp() {
        BootstrapMode.clear();
    }

    @Test
    void shouldThrowExceptionWhenEnvironmentIsNull() {
        assertThatThrownBy(() -> processor.postProcessEnvironment(null, new SpringApplication()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("environment cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenApplicationIsNull() {
        assertThatThrownBy(() -> processor.postProcessEnvironment(mock(ConfigurableEnvironment.class), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("application cannot be null");
    }

    @Test
    void shouldNotAddProfilesWhenDisabled() {
        MockEnvironment environment = new MockEnvironment().withProperty("rose.bootstrap.profiles.enabled", "false");
        SpringApplication application = new SpringApplication();

        processor.postProcessEnvironment(environment, application);

        assertThat(environment.getActiveProfiles()).isEmpty();
    }

    // DEV

    @Test
    void shouldAddDefaultProfilesWhenDevMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).contains("dev");
                });
    }

    @Test
    void shouldAddCustomProfilesWhenDevMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .withPropertyValues("rose.dev.profiles=custom-dev")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).contains("custom-dev");
                });
    }

    @Test
    void shouldNotAddDuplicateProfilesWhenDevMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .withPropertyValues("spring.profiles.active=custom-dev")
                .withPropertyValues("rose.dev.profiles=custom-dev")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).hasSize(1);
                    assertThat(context.getEnvironment().getActiveProfiles()).contains("custom-dev");
                });
    }

    @Test
    void shouldNotAddEmptyProfileWhenDevMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .withPropertyValues("rose.dev.profiles=")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).doesNotContain("dev");
                });
    }

    @Test
    void shouldAddMultipleProfilesWhenDevMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .withPropertyValues("rose.dev.profiles=dev1,dev2,dev3")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles())
                            .contains("dev1", "dev2", "dev3");
                });
    }

    @Test
    void shouldFilterEmptyProfilesInList() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .withPropertyValues("rose.dev.profiles=dev1,,dev2, ,dev3")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles())
                            .contains("dev1", "dev2", "dev3")
                            .hasSize(3);
                });
    }

    // TEST

    @Test
    void shouldAddDefaultProfilesWhenTestMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=test")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).contains("test");
                });
    }

    @Test
    void shouldAddCustomProfilesWhenTestMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=test")
                .withPropertyValues("rose.test.profiles=custom-test")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).contains("custom-test");
                });
    }

    @Test
    void shouldNotAddDuplicateProfilesWhenTestMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=test")
                .withPropertyValues("spring.profiles.active=custom-test")
                .withPropertyValues("rose.test.profiles=custom-test")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).hasSize(1);
                    assertThat(context.getEnvironment().getActiveProfiles()).contains("custom-test");
                });
    }

    @Test
    void shouldNotAddEmptyProfileWhenTestMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=test")
                .withPropertyValues("rose.test.profiles=")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).doesNotContain("test");
                });
    }

    @Test
    void shouldAddMultipleProfilesWhenTestMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=test")
                .withPropertyValues("rose.test.profiles=test1, \ntest2,test3")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles())
                            .contains("test1", "test2", "test3");
                });
    }

    // PRODUCTION

    @Test
    void shouldAddNoProfileWhenProductionMode() {
        new ApplicationContextRunner()
                .withSystemProperties("rose.bootstrap.mode=prod")
                .withInitializer(context -> {
                    SpringApplication application = new SpringApplication(TestConfig.class);
                    application.setMainApplicationClass(context.getClass());
                    processor.postProcessEnvironment(context.getEnvironment(), application);
                })
                .run(context -> {
                    assertThat(context.getEnvironment().getActiveProfiles()).isEmpty();
                });
    }

    @Configuration
    static class TestConfig {
    }

}
