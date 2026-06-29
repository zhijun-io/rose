package io.zhijun.spring.core.env;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import io.zhijun.spring.core.env.event.PropertySourceChangedEvent;
import io.zhijun.spring.core.env.listener.EnvironmentListener;

class ListenableMutablePropertySourcesTests {

    @Test
    void shouldPublishChangeEvents() {
        MutablePropertySources delegate = new MutablePropertySources();
        StaticApplicationContext context = new StaticApplicationContext();
        List<PropertySourceChangedEvent> events = new ArrayList<PropertySourceChangedEvent>();
        EnvironmentListener listener = new EnvironmentListener() {
            @Override
            public void onPropertySourceChanged(PropertySourceChangedEvent event) {
                events.add(event);
            }
        };
        ListenableMutablePropertySources propertySources =
                new ListenableMutablePropertySources(delegate, context, Collections.singletonList(listener));

        Map<String, Object> first = new HashMap<String, Object>();
        first.put("a", "b");
        Map<String, Object> second = new HashMap<String, Object>();
        second.put("a", "c");
        propertySources.addLast(new MapPropertySource("demo", first));
        propertySources.replace("demo", new MapPropertySource("demo", second));
        propertySources.remove("demo");

        assertThat(events).hasSize(3);
        assertThat(events.get(0).getKind()).isEqualTo(PropertySourceChangedEvent.Kind.ADDED);
        assertThat(events.get(1).getKind()).isEqualTo(PropertySourceChangedEvent.Kind.REPLACED);
        assertThat(events.get(2).getKind()).isEqualTo(PropertySourceChangedEvent.Kind.REMOVED);
    }
}
