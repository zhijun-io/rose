package io.zhijun.spring.boot.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultConfigLoaderTests {

    private final DefaultConfigLoader loader =
            new DefaultConfigLoader(new PathMatchingResourcePatternResolver(new DefaultResourceLoader()));

    @Test
    void shouldLoadCoreDefaultsFromClasspath() {
        Map<String, Object> properties =
                loader.load(DefaultConfigEnvironmentPostProcessor.DEFAULT_PROPERTIES_PATTERN);

        assertThat(properties).containsEntry("server.shutdown", "graceful");
        assertThat(properties).containsEntry("spring.lifecycle.timeout-per-shutdown-phase", "60s");
    }

    @Test
    void shouldLoadYamlDefaultsFromClasspath() {
        Map<String, Object> properties =
                loader.load(DefaultConfigEnvironmentPostProcessor.DEFAULT_YML_PATTERN);

        assertThat(properties).containsEntry("rose.test.yaml-key", "from-yaml");
        assertThat(properties).containsEntry("server.port", "18080");
    }

    @Test
    void shouldFlattenYamlListsToIndexedKeys() {
        Map<String, Object> properties = loader.load("classpath:config/default/lists.yml");

        assertThat(properties).containsEntry("rose.test.tags[0]", "alpha");
        assertThat(properties).containsEntry("rose.test.tags[1]", "beta");
    }

    @Test
    void shouldMergeMultiDocumentYaml() {
        Map<String, Object> properties = loader.load("classpath:config/default/multi-doc.yaml");

        assertThat(properties).containsEntry("rose.test.first", "one");
        assertThat(properties).containsEntry("rose.test.second", "two");
    }

    @Test
    void shouldLoadAllDefaultPatternsFromClasspath() {
        Map<String, Object> properties =
                loader.load(DefaultConfigEnvironmentPostProcessor.DEFAULT_LOCATION_PATTERNS);

        assertThat(properties).containsEntry("server.shutdown", "graceful");
        assertThat(properties).containsEntry("rose.test.yaml-key", "from-yaml");
        assertThat(properties).containsEntry("rose.test.meta-inf-default-key", "from-meta-inf");
    }

    @Test
    void shouldLoadMetaInfDefaultPropertiesFromClasspath() {
        Map<String, Object> properties =
                loader.load(DefaultConfigEnvironmentPostProcessor.META_INF_DEFAULT_PROPERTIES_PATTERN);

        assertThat(properties).containsEntry("rose.test.meta-inf-default-key", "from-meta-inf");
    }

    @Test
    void shouldMergeLaterResourceOverEarlierForSameKey() {
        Map<String, Object> properties = loader.load(
                "classpath:config/default/merge-a.properties", "classpath:config/default/merge-b.properties");

        assertThat(properties).containsEntry("rose.test.key", "b");
    }

    @Test
    void shouldAccumulateAutoConfigurationExcludeAcrossResources() {
        Map<String, Object> properties = loader.load(
                "classpath:config/default/merge-exclude-a.properties",
                "classpath:config/default/merge-exclude-b.properties");

        assertThat(properties)
                .containsEntry("rose.autoconfigure.exclude", "com.example.ExcludeFromA,com.example.ExcludeFromB");
    }
}
