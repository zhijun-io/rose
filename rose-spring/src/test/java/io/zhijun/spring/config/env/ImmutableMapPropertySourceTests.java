package io.zhijun.spring.config.env;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutableMapPropertySourceTests {

    @Test
    void shouldCreateWithHashMap() {
        Map<String, Object> source = new HashMap<>();
        source.put("key", "value");
        ImmutableMapPropertySource ps = new ImmutableMapPropertySource("test", source);
        assertThat(ps.getName()).isEqualTo("test");
        assertThat(ps.getProperty("key")).isEqualTo("value");
    }

    @Test
    void shouldCreateWithLinkedHashMap() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("a", "1");
        source.put("b", "2");
        ImmutableMapPropertySource ps = new ImmutableMapPropertySource("linked", source);
        assertThat(ps.getProperty("a")).isEqualTo("1");
        assertThat(ps.getProperty("b")).isEqualTo("2");
    }

    @Test
    void shouldCreateWithTreeMap() {
        Map<String, Object> source = new TreeMap<>();
        source.put("z", "last");
        source.put("a", "first");
        ImmutableMapPropertySource ps = new ImmutableMapPropertySource("tree", source);
        assertThat(ps.getProperty("a")).isEqualTo("first");
        assertThat(ps.getProperty("z")).isEqualTo("last");
    }

    @Test
    void shouldBeImmutable() {
        Map<String, Object> source = new HashMap<>();
        source.put("key", "value");
        ImmutableMapPropertySource ps = new ImmutableMapPropertySource("immutable", source);

        assertThatThrownBy(() -> ps.getSource().put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldNotBeAffectedByOriginalMapMutation() {
        Map<String, Object> source = new HashMap<>();
        source.put("key", "original");
        ImmutableMapPropertySource ps = new ImmutableMapPropertySource("safe", source);
        source.put("key", "modified");
        assertThat(ps.getProperty("key")).isEqualTo("original");
    }

    @Test
    void shouldContainProperty() {
        ImmutableMapPropertySource ps = new ImmutableMapPropertySource("test",
                Collections.singletonMap("exists", "yes"));
        assertThat(ps.containsProperty("exists")).isTrue();
        assertThat(ps.containsProperty("missing")).isFalse();
    }
}
