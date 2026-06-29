package io.zhijun.spring.core.env.event.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

class PropertySourceDiffSupportTest {

    @Test
    void diffReplacedReturnsOnlyChangedKey() {
        Map<String, Object> oldMap = new HashMap<String, Object>();
        oldMap.put("a", "1");
        oldMap.put("b", "2");
        Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.put("a", "1");
        newMap.put("b", "changed");

        Set<String> diff = PropertySourceDiffSupport.diffReplaced(
                new MapPropertySource("demo", oldMap), new MapPropertySource("demo", newMap));

        assertThat(diff).containsExactly("b");
    }

    @Test
    void diffReplacedReturnsEmptyWhenUnchanged() {
        Map<String, Object> map = Collections.singletonMap("a", "1");

        Set<String> diff = PropertySourceDiffSupport.diffReplaced(
                new MapPropertySource("demo", map), new MapPropertySource("demo", map));

        assertThat(diff).isEmpty();
    }

    @Test
    void diffReplacedIgnoresNullValuesWhenBothSidesNull() {
        Map<String, Object> oldMap = new HashMap<String, Object>();
        oldMap.put("nullable", null);
        Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.put("nullable", null);

        Set<String> diff = PropertySourceDiffSupport.diffReplaced(
                new MapPropertySource("demo", oldMap), new MapPropertySource("demo", newMap));

        assertThat(diff).isEmpty();
    }

    @Test
    void diffReplacedDetectsRemovedKey() {
        Map<String, Object> oldMap = new HashMap<String, Object>();
        oldMap.put("kept", "1");
        oldMap.put("removed", "x");
        Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.put("kept", "1");

        Set<String> diff = PropertySourceDiffSupport.diffReplaced(
                new MapPropertySource("demo", oldMap), new MapPropertySource("demo", newMap));

        assertThat(diff).containsExactly("removed");
    }

    @Test
    void getPropertyNamesReturnsEmptyForNonEnumerableSource() {
        PropertySource<Object> source = new PropertySource<Object>("demo", new Object()) {
            @Override
            public Object getProperty(String name) {
                return null;
            }
        };

        assertThat(PropertySourceDiffSupport.getPropertyNames(source)).isEmpty();
        assertThat(PropertySourceDiffSupport.diffReplaced(source, source)).isEmpty();
        assertThat(PropertySourceDiffSupport.keysAdded(source)).isEmpty();
        assertThat(PropertySourceDiffSupport.keysRemoved(source)).isEmpty();
    }

    @Test
    void keysAddedReturnsAllKeys() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("x", "1");
        map.put("y", "2");

        assertThat(PropertySourceDiffSupport.keysAdded(new MapPropertySource("demo", map)))
                .containsExactly("x", "y");
    }

    @Test
    void keysRemovedReturnsAllKeys() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("x", "1");
        map.put("y", "2");

        assertThat(PropertySourceDiffSupport.keysRemoved(new MapPropertySource("demo", map)))
                .containsExactly("x", "y");
    }
}
