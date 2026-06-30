package io.zhijun.spring.core.env;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.env.MockPropertySource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PropertySourcesUtilsTests {

    @Test
    void shouldNormalizePrefixWithoutDot() {
        assertThat(PropertySourcesUtils.normalizePrefix("prefix")).isEqualTo("prefix.");
    }

    @Test
    void shouldNormalizePrefixWithDot() {
        assertThat(PropertySourcesUtils.normalizePrefix("prefix.")).isEqualTo("prefix.");
    }

    @Test
    void shouldNormalizeEmptyPrefix() {
        assertThat(PropertySourcesUtils.normalizePrefix("")).isEqualTo(".");
    }

    @Test
    void shouldGetDefaultProperties() {
        MockEnvironment env = new MockEnvironment();
        MapPropertySource defaultPs = new MapPropertySource("defaultProperties",
                new java.util.LinkedHashMap<>(Collections.singletonMap("key1", "value1")));
        env.getPropertySources().addLast(defaultPs);
        Map<String, Object> defaults = PropertySourcesUtils.getDefaultProperties(env);
        assertThat(defaults).isNotNull();
        assertThat(defaults.get("key1")).isEqualTo("value1");
    }

    @Test
    void shouldReturnNullWhenNoDefaultProperties() {
        MockEnvironment env = new MockEnvironment();
        env.getPropertySources().remove("defaultProperties");
        Map<String, Object> defaults = PropertySourcesUtils.getDefaultProperties(env);
        assertThat(defaults).isNull();
    }

    @Test
    void shouldFindPropertyNamesWithFilter() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("server.port", "8080");
        env.setProperty("server.host", "localhost");
        env.setProperty("app.name", "test");

        Set<String> result = PropertySourcesUtils.findPropertyNames(env, name -> name.startsWith("server."));
        assertThat(result).containsExactlyInAnyOrder("server.port", "server.host");
    }

    @Test
    void shouldReturnEmptySetWhenNoMatch() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("app.name", "test");

        Set<String> result = PropertySourcesUtils.findPropertyNames(env, name -> name.startsWith("db."));
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetSubPropertiesAsString() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("datasource.url", "jdbc:h2:mem");
        env.setProperty("datasource.user", "sa");
        env.setProperty("app.name", "test");

        Map<String, String> result = PropertySourcesUtils.getSubProperties(env.getPropertySources(), "datasource");
        assertThat(result)
                .containsEntry("url", "jdbc:h2:mem")
                .containsEntry("user", "sa");
        assertThat(result).doesNotContainKey("name");
    }

    @Test
    void shouldGetSubPropertiesAsObject() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("app.feature.x", "true");
        env.setProperty("app.feature.y", "false");

        Map<String, Object> result = PropertySourcesUtils.getSubProperties(
                env.getPropertySources(), env, "app.feature");
        assertThat(result)
                .containsEntry("x", "true")
                .containsEntry("y", "false");
    }

    @Test
    void shouldHandleNonEnumerablePropertySources() {
        MockEnvironment env = new MockEnvironment();
        MockPropertySource nonEnumerable = new MockPropertySource("non-enumerable") {
            @Override
            public String[] getPropertyNames() {
                return new String[0];
            }
        };
        env.getPropertySources().addFirst(nonEnumerable);

        Map<String, String> result = PropertySourcesUtils.getSubProperties(env.getPropertySources(), "any");
        assertThat(result).isEmpty();
    }
}
