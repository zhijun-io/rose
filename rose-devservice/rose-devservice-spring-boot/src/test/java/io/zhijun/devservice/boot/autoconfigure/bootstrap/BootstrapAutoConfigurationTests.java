package io.zhijun.devservice.boot.autoconfigure.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Unit test for {@link BootstrapAutoConfiguration}.
 */
class BootstrapAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(BootstrapAutoConfiguration.class));

    @Test
    void registersBootstrapPropertiesBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(BootstrapProperties.class);
            assertThat(context).hasSingleBean(BootstrapDevProperties.class);
            assertThat(context).hasSingleBean(BootstrapTestProperties.class);
        });
    }
}
