package io.zhijun.local.tests;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StreamUtils;

import io.zhijun.local.bootstrap.BootstrapMode;
import io.zhijun.boot.env.defaults.DefaultConfigPropertiesEnvironmentPostProcessor;
import org.testcontainers.containers.GenericContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base integration tests for dev services auto-configuration.
 */
public abstract class BaseLocalServiceAutoConfigurationIT {

    @FunctionalInterface
    protected interface ThrowingConsumer<T> {

        void accept(T value) throws Exception;

    }

    @FunctionalInterface
    protected interface ThrowingBiConsumer<T, U> {

        void accept(T first, U second) throws Exception;

    }

    static {
        DockerTestSupport.configureIfNeeded();
    }

    @TempDir
    protected static Path testMountDir;

    protected abstract ApplicationContextRunner getContextRunner();

    protected abstract Class<?> getAutoConfigurationClass();

    protected abstract Class<?> getContainerClass();

    protected abstract String getServiceName();

    @BeforeEach
    void setUp() {
        BootstrapMode.clear();
    }

    @Test
    void autoConfigurationNotActivatedWhenGloballyDisabled() {
        getContextRunner()
                .withPropertyValues("rose.local.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(getContainerClass()));
    }

    @Test
    void autoConfigurationNotActivatedWhenDisabled() {
        getContextRunner()
                .withPropertyValues(String.format("rose.local.%s.enabled=false", getServiceName()))
                .run(context -> assertThat(context).doesNotHaveBean(getContainerClass()));
    }

    @Test
    void containerAvailableInTestMode() {
        getContextRunner()
                .withSystemProperties("rose.bootstrap.mode=test")
                .run(context -> {
                    assertThat(context).hasSingleBean(getContainerClass());
                    GenericContainer<?> container = (GenericContainer<?>) context.getBean(getContainerClass());
                    assertThat(container.isShouldBeReused()).isFalse();
                });
    }

    @Test
    void containerWithRestartScope() {
        getContextRunner()
                .withClassLoader(this.getClass().getClassLoader())
                .withInitializer(context ->
                        context.getBeanFactory().registerScope("restart", new SimpleThreadScope()))
                .run(context -> {
                    assertThat(context).hasSingleBean(getContainerClass());
                    String[] beanNames = context.getBeanFactory().getBeanNamesForType(getContainerClass());
                    assertThat(beanNames).hasSize(1);
                    assertThat(context.getBeanFactory().getBeanDefinition(beanNames[0]).getScope())
                            .isEqualTo("restart");
                });
    }

    protected void assertThatHasSingletonScope(AssertableApplicationContext context) {
        String[] beanNames = context.getBeanFactory().getBeanNamesForType(getContainerClass());
        assertThat(beanNames).hasSize(1);
        assertThat(context.getBeanFactory().getBeanDefinition(beanNames[0]).getScope())
                .isEqualTo("singleton");
    }

    protected <T extends GenericContainer<?>> void assertContainerAvailableInDevMode(
            Class<T> containerClass, String expectedImageName, ThrowingConsumer<T> assertions) {
        getContextRunner()
                .withSystemProperties("rose.bootstrap.mode=dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(containerClass);
                    T container = context.getBean(containerClass);
                    assertThat(container.getDockerImageName()).contains(expectedImageName);
                    assertThat(container.getNetworkAliases()).hasSize(1);
                    assertThat(container.isShouldBeReused()).isTrue();
                    assertions.accept(container);
                    assertThatHasSingletonScope(context);
                });
    }

    protected <T extends GenericContainer<?>> void assertContainerAvailableWithDefaultConfiguration(
            Class<T> containerClass, String expectedImageName, ThrowingConsumer<T> assertions) {
        getContextRunner()
                .run(context -> {
                    assertThat(context).hasSingleBean(containerClass);
                    T container = context.getBean(containerClass);
                    assertThat(container.getDockerImageName()).contains(expectedImageName);
                    assertThat(container.getNetworkAliases()).hasSize(1);
                    assertThat(container.isShouldBeReused()).isFalse();
                    assertions.accept(container);
                    assertThatHasSingletonScope(context);
                });
    }

    protected <T extends GenericContainer<?>> void assertContainerConfigurationApplied(
            Class<T> containerClass, String[] properties, ThrowingConsumer<T> assertions) {
        assertContainerConfigurationApplied(containerClass, properties, (context, container) -> assertions.accept(container));
    }

    protected <T extends GenericContainer<?>> void assertContainerConfigurationDeclared(
            Class<T> containerClass, String[] properties, ThrowingConsumer<T> assertions) {
        assertContainerConfigurationDeclared(containerClass, properties, (context, container) -> assertions.accept(container));
    }

    protected <T extends GenericContainer<?>> void assertContainerConfigurationDeclared(
            Class<T> containerClass,
            String[] properties,
            ThrowingBiConsumer<AssertableApplicationContext, T> assertions) {
        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    T container = context.getBean(containerClass);
                    assertThatConfigurationIsDeclared(container);
                    assertions.accept(context, container);
                    assertThatHasSingletonScope(context);
                });
    }

    protected <T extends GenericContainer<?>> void assertContainerConfigurationApplied(
            Class<T> containerClass,
            String[] properties,
            ThrowingBiConsumer<AssertableApplicationContext, T> assertions) {
        getContextRunner()
                .withPropertyValues(properties)
                .run(context -> {
                    T container = context.getBean(containerClass);
                    container.start();
                    try {
                        assertThatConfigurationIsApplied(container);
                        assertions.accept(context, container);
                    }
                    finally {
                        container.stop();
                    }
                });
    }

    protected static void assertThatConfigurationIsDeclared(GenericContainer<?> container) {
        assertThat(container.getEnv()).contains("KEY=value");
        assertThat(container.getNetworkAliases()).contains("network1");
        assertThat(container.getBinds()).anyMatch(b -> b.getPath().equals(testMountDir.toAbsolutePath().toString()));
        assertThat(container.getBinds()).anyMatch(b -> b.getVolume().getPath().equals("/rose"));
    }

    protected String[] commonConfigurationProperties() {
        String prefix = "rose.local." + getServiceName();
        return new String[] {
                prefix + ".environment.KEY=value",
                prefix + ".network-aliases=network1",
                prefix + ".resources[0].source-path=test-resource.txt",
                prefix + ".resources[0].container-path=/tmp/test-resource.txt",
                prefix + ".volumes[0].host-path=" + testMountDir.toAbsolutePath(),
                prefix + ".volumes[0].container-path=/rose"
        };
    }

    protected static void assertThatConfigurationIsApplied(GenericContainer<?> container) throws Exception {
        assertThatConfigurationIsDeclared(container);
        assertThat(container.getCurrentContainerInfo().getState().getStatus()).isEqualTo("running");

        String mappedResourceContent = container.copyFileFromContainer(
                "/tmp/test-resource.txt",
                inputStream -> StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
        assertThat(mappedResourceContent).isNotEmpty();
    }

    protected static ApplicationContextRunner defaultContextRunner(Class<?> autoConfigurationClass) {
        return new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader(RestartScope.class))
                .withInitializer(context -> {
                    if (context.getEnvironment() instanceof ConfigurableEnvironment) {
                        new DefaultConfigPropertiesEnvironmentPostProcessor()
                                .postProcessEnvironment((ConfigurableEnvironment) context.getEnvironment(),
                                        new SpringApplication());
                    }
                })
                .withConfiguration(AutoConfigurations.of(autoConfigurationClass));
    }
}
