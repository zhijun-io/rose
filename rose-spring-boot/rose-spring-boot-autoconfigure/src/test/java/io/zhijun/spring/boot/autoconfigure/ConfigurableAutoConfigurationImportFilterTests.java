package io.zhijun.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConfigurableAutoConfigurationImportFilterTests {

    private final ConfigurableAutoConfigurationImportFilter filter = new ConfigurableAutoConfigurationImportFilter();

    @Test
    void shouldMatchAllByDefault() {
        filter.setEnvironment(new MockEnvironment());
        String[] classes = {"com.example.MyAutoConfig", "com.example.OtherConfig"};
        AutoConfigurationMetadata metadata = mock(AutoConfigurationMetadata.class);
        boolean[] result = filter.match(classes, metadata);
        assertThat(result).containsExactly(true, true);
    }

    @Test
    void shouldExcludeConfiguredClasses() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME,
                "com.example.MyAutoConfig");
        filter.setEnvironment(env);

        String[] classes = {"com.example.MyAutoConfig", "com.example.OtherConfig"};
        AutoConfigurationMetadata metadata = mock(AutoConfigurationMetadata.class);
        boolean[] result = filter.match(classes, metadata);
        assertThat(result).containsExactly(false, true);
    }

    @Test
    void shouldExcludeMultipleConfiguredClasses() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME,
                "com.example.MyAutoConfig,com.example.OtherConfig");
        filter.setEnvironment(env);

        String[] classes = {"com.example.MyAutoConfig", "com.example.OtherConfig", "com.example.ThirdConfig"};
        AutoConfigurationMetadata metadata = mock(AutoConfigurationMetadata.class);
        boolean[] result = filter.match(classes, metadata);
        assertThat(result).containsExactly(false, false, true);
    }

    @Test
    void shouldReturnTrueForEmptyClassName() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME,
                "com.example.MyAutoConfig");
        filter.setEnvironment(env);

        String[] classes = {"", null};
        AutoConfigurationMetadata metadata = mock(AutoConfigurationMetadata.class);
        boolean[] result = filter.match(classes, metadata);
        assertThat(result).containsExactly(true, true);
    }

    @Test
    void shouldHaveHighPrecedence() {
        assertThat(filter.getOrder()).isLessThan(0);
    }

    @Test
    void shouldGetExcludedClassesFromEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.autoconfigure.exclude", "com.example.TestConfig");
        assertThat(ConfigurableAutoConfigurationImportFilter
                .getExcludedAutoConfigurationClasses(env)).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoExclusions() {
        MockEnvironment env = new MockEnvironment();
        assertThat(ConfigurableAutoConfigurationImportFilter
                .getExcludedAutoConfigurationClasses(env)).isEmpty();
    }

    @Test
    void shouldAddExcludedClassProgrammatically() {
        MockEnvironment env = new MockEnvironment();
        ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(env, "com.example.DynamicExclude");
        assertThat(ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses(env))
                .contains("com.example.DynamicExclude");
    }
}
