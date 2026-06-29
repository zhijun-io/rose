package io.zhijun.spring.core.propertysource.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import io.zhijun.spring.core.propertysource.PropertySourceMaps;
import org.junit.jupiter.api.Test;

class PropertySourceMapsTests {

    @Test
    void shouldFlattenNestedMaps() {
        Map<String, Object> source = new LinkedHashMap<String, Object>();
        Map<String, Object> nested = new LinkedHashMap<String, Object>();
        nested.put("enabled", true);
        source.put("app", nested);
        source.put("name", "rose");

        Map<String, Object> flattened = PropertySourceMaps.flatten(source);

        assertThat(flattened).containsEntry("app.enabled", true);
        assertThat(flattened).containsEntry("name", "rose");
    }

    @Test
    void shouldFlattenListsWithIndexedKeys() {
        Map<String, Object> source = new LinkedHashMap<String, Object>();
        source.put("tags", Arrays.asList("a", "b"));

        Map<String, Object> flattened = PropertySourceMaps.flatten(source);

        assertThat(flattened).containsEntry("tags[0]", "a");
        assertThat(flattened).containsEntry("tags[1]", "b");
    }

    @Test
    void shouldFlattenMapsInsideLists() {
        Map<String, Object> host = new LinkedHashMap<String, Object>();
        host.put("name", "primary");
        host.put("port", 5432);
        Map<String, Object> source = new LinkedHashMap<String, Object>();
        source.put("servers", Collections.singletonList(host));

        Map<String, Object> flattened = PropertySourceMaps.flatten(source);

        assertThat(flattened).containsEntry("servers[0].name", "primary");
        assertThat(flattened).containsEntry("servers[0].port", 5432);
    }

    @Test
    void shouldNormalizePropertyValuesToStrings() {
        assertThat(PropertySourceMaps.normalizePropertyValue(18080)).isEqualTo("18080");
        assertThat(PropertySourceMaps.normalizePropertyValue(true)).isEqualTo("true");
        assertThat(PropertySourceMaps.normalizePropertyValue("60s")).isEqualTo("60s");
        assertThat(PropertySourceMaps.normalizePropertyValue(null)).isNull();
    }
}
