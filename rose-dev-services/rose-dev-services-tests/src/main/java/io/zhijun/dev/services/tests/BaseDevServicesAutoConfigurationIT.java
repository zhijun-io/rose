package io.zhijun.dev.services.tests;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.GenericContainer;

import io.zhijun.boot.bootstrap.BootstrapMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base integration tests for dev services auto-configuration.
 */
public abstract class BaseDevServicesAutoConfigurationIT {

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
                .withPropertyValues("rose.dev.services.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(getContainerClass()));
    }

    @Test
    void autoConfigurationNotActivatedWhenDisabled() {
        getContextRunner()
                .withPropertyValues(String.format("rose.dev.services.%s.enabled=false", getServiceName()))
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

    protected String[] commonConfigurationProperties() {
        String prefix = "rose.dev.services." + getServiceName();
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
        assertThat(container.getEnv()).contains("KEY=value");
        assertThat(container.getNetworkAliases()).contains("network1");
        assertThat(container.getCurrentContainerInfo().getState().getStatus()).isEqualTo("running");

        String mappedResourceContent = container.copyFileFromContainer(
                "/tmp/test-resource.txt",
                inputStream -> StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
        assertThat(mappedResourceContent).isNotEmpty();

        assertThat(container.getBinds()).anyMatch(b -> b.getPath().equals(testMountDir.toAbsolutePath().toString()));
        assertThat(container.getBinds()).anyMatch(b -> b.getVolume().getPath().equals("/rose"));
    }

    protected static ApplicationContextRunner defaultContextRunner(Class<?> autoConfigurationClass) {
        return new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader(RestartScope.class))
                .withConfiguration(AutoConfigurations.of(autoConfigurationClass));
    }
}
