package io.zhijun.spring.core.env.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;

class PropertySourcesChangedEventDiffTest {

    @Test
    void getChangedKeysUnionsAddedAndRemoved() {
        StaticApplicationContext context = new StaticApplicationContext();
        Map<String, Object> added = Collections.singletonMap("added.key", "v");
        Map<String, Object> removed = Collections.singletonMap("removed.key", "v");

        PropertySourcesChangedEvent event = new PropertySourcesChangedEvent(
                context,
                Arrays.asList(
                        PropertySourceChangedEvent.added(context, new MapPropertySource("added", added)),
                        PropertySourceChangedEvent.removed(context, new MapPropertySource("removed", removed))));

        Set<String> keys = event.getChangedKeys();

        assertThat(keys).containsExactlyInAnyOrder("added.key", "removed.key");
    }

    @Test
    void getChangedKeysUsesDiffForReplaced() {
        StaticApplicationContext context = new StaticApplicationContext();
        Map<String, Object> oldMap = new HashMap<String, Object>();
        oldMap.put("same", "1");
        oldMap.put("changed", "old");
        Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.put("same", "1");
        newMap.put("changed", "new");

        PropertySourcesChangedEvent event = new PropertySourcesChangedEvent(
                context,
                Collections.singletonList(PropertySourceChangedEvent.replaced(
                        context, new MapPropertySource("demo", newMap), new MapPropertySource("demo", oldMap))));

        assertThat(event.getChangedKeys()).containsExactly("changed");
    }
}
