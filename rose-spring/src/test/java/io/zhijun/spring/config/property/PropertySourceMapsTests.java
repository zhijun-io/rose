package io.zhijun.spring.config.property;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PropertySourceMaps} 单元测试。
 */
class PropertySourceMapsTests {

    @Test
    void shouldFlattenNestedMap() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("spring.application.name", "my-app");
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("host", "localhost");
        nested.put("port", 8080);
        source.put("server", nested);

        Map<String, Object> result = PropertySourceMaps.flatten(source);

        assertThat(result)
                .containsEntry("spring.application.name", "my-app")
                .containsEntry("server.host", "localhost")
                .containsEntry("server.port", 8080);
        assertThat(result).hasSize(3);
    }

    @Test
    void shouldFlattenDeeplyNestedMap() {
        Map<String, Object> source = new LinkedHashMap<>();
        Map<String, Object> datasource = new LinkedHashMap<>();
        Map<String, Object> pool = new LinkedHashMap<>();
        pool.put("max-size", 10);
        pool.put("min-idle", 2);
        datasource.put("pool", pool);
        datasource.put("url", "jdbc:h2:mem:test");
        source.put("datasource", datasource);

        Map<String, Object> result = PropertySourceMaps.flatten(source);

        assertThat(result)
                .containsEntry("datasource.pool.max-size", 10)
                .containsEntry("datasource.pool.min-idle", 2)
                .containsEntry("datasource.url", "jdbc:h2:mem:test");
    }

    @Test
    void shouldFlattenListValues() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("tags", Arrays.asList("a", "b", "c"));

        Map<String, Object> result = PropertySourceMaps.flatten(source);

        assertThat(result)
                .containsEntry("tags[0]", "a")
                .containsEntry("tags[1]", "b")
                .containsEntry("tags[2]", "c");
    }

    @Test
    void shouldFlattenArrayValues() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("ports", new int[]{8080, 9090});

        Map<String, Object> result = PropertySourceMaps.flatten(source);

        assertThat(result)
                .containsEntry("ports[0]", 8080)
                .containsEntry("ports[1]", 9090);
    }

    @Test
    void shouldFlattenNestedListInMap() {
        Map<String, Object> source = new LinkedHashMap<>();
        Map<String, Object> servers = new LinkedHashMap<>();
        servers.put("hosts", Arrays.asList("node1", "node2"));
        source.put("cluster", servers);

        Map<String, Object> result = PropertySourceMaps.flatten(source);

        assertThat(result)
                .containsEntry("cluster.hosts[0]", "node1")
                .containsEntry("cluster.hosts[1]", "node2");
    }

    @Test
    void shouldReturnEmptyMapForEmptyInput() {
        Map<String, Object> result = PropertySourceMaps.flatten(new LinkedHashMap<>());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnNullForNullValue() {
        assertThat(PropertySourceMaps.normalizePropertyValue(null)).isNull();
    }

    @Test
    void shouldReturnStringForStringValue() {
        assertThat(PropertySourceMaps.normalizePropertyValue("hello")).isEqualTo("hello");
    }

    @Test
    void shouldReturnStringForBooleanValue() {
        assertThat(PropertySourceMaps.normalizePropertyValue(true)).isEqualTo("true");
    }

    @Test
    void shouldReturnStringForIntegerValue() {
        assertThat(PropertySourceMaps.normalizePropertyValue(42)).isEqualTo("42");
    }

    @Test
    void shouldReturnToStringForNonPrimitiveValue() {
        assertThat(PropertySourceMaps.normalizePropertyValue(new Object() {
            @Override
            public String toString() {
                return "custom";
            }
        })).isEqualTo("custom");
    }
}
