package io.zhijun.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

class ConfigurableAutoConfigurationImportFilterTests {

    private static final AutoConfigurationMetadata METADATA = new AutoConfigurationMetadata() {
        @Override
        public boolean wasProcessed(String className) {
            return false;
        }

        @Override
        public Integer getInteger(String className, String key) {
            return null;
        }

        @Override
        public Integer getInteger(String className, String key, Integer defaultValue) {
            return defaultValue;
        }

        @Override
        public Set<String> getSet(String className, String key) {
            return null;
        }

        @Override
        public Set<String> getSet(String className, String key, Set<String> defaultValue) {
            return defaultValue;
        }

        @Override
        public String get(String className, String key) {
            return null;
        }

        @Override
        public String get(String className, String key, String defaultValue) {
            return defaultValue;
        }
    };

    private final ConfigurableAutoConfigurationImportFilter filter = new ConfigurableAutoConfigurationImportFilter();

    @Test
    void shouldExcludeConfiguredAutoConfigurationClasses() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME,
                        "com.example.FooAutoConfiguration,com.example.BarAutoConfiguration");
        filter.setEnvironment(environment);

        boolean[] results = filter.match(
                new String[] {"com.example.FooAutoConfiguration", "com.example.BazAutoConfiguration"}, METADATA);

        assertThat(results).containsExactly(false, true);
    }

    @Test
    void shouldMergeExclusionsFromMultiplePropertySources() {
        MockEnvironment environment = new MockEnvironment();
        environment
                .getPropertySources()
                .addLast(new MapPropertySource(
                        "module-a",
                        Collections.singletonMap(
                                ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME,
                                "com.example.A")));
        environment
                .getPropertySources()
                .addLast(new MapPropertySource(
                        "module-b",
                        Collections.singletonMap(
                                ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME,
                                "com.example.B")));
        filter.setEnvironment(environment);

        boolean[] results = filter.match(new String[] {"com.example.A", "com.example.B", "com.example.C"}, METADATA);

        assertThat(results).containsExactly(false, false, true);
    }

    @Test
    void shouldSupportIndexedExcludePropertyFromBinder() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(
                ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME + "[0]",
                "com.example.First");
        environment.setProperty(
                ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME + "[1]",
                "com.example.Second");
        filter.setEnvironment(environment);

        Set<String> excluded =
                ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses(environment);

        assertThat(excluded).containsExactly("com.example.First", "com.example.Second");
    }

    @Test
    void shouldAddExcludedClassesProgrammatically() {
        MockEnvironment environment = new MockEnvironment();
        ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClasses(
                environment, Arrays.asList("com.example.RuntimeA", "com.example.RuntimeB"));
        filter.setEnvironment(environment);

        boolean[] results = filter.match(new String[] {"com.example.RuntimeA", "com.example.Other"}, METADATA);

        assertThat(results).containsExactly(false, true);
        assertThat(environment.getProperty(
                        ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME))
                .isEqualTo("com.example.RuntimeA,com.example.RuntimeB");
    }

    @Test
    void shouldResolvePlaceholdersInExcludeProperty() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("rose.exclude.target", "com.example.Resolved")
                .withProperty(
                        ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME,
                        "${rose.exclude.target}");
        filter.setEnvironment(environment);

        boolean[] results = filter.match(new String[] {"com.example.Resolved"}, METADATA);

        assertThat(results).containsExactly(false);
    }
}
