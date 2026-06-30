package io.zhijun.spring.env;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;

import io.zhijun.spring.env.PropertySourcesChangedEvent;

class ListenableConfigurableEnvironmentTests {

    @Test
    void shouldPublishPropertySourceChangeEvents() {
        StaticApplicationContext context = new StaticApplicationContext();
        List<PropertySourcesChangedEvent> events = new ArrayList<PropertySourcesChangedEvent>();
        EnvironmentListener listener = new EnvironmentListener() {
            @Override
            public void onPropertySourcesChanged(PropertySourcesChangedEvent event) {
                events.add(event);
            }
        };

        ListenableConfigurableEnvironment environment = new ListenableConfigurableEnvironment(
                context.getEnvironment(), context, Collections.singletonList(listener));

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("a", "b");
        environment.getPropertySources().addLast(new MapPropertySource("demo", values));

        assertThat(events).hasSize(1);
        assertThat(environment.containsProperty("a")).isTrue();
        assertThat(environment.getProperty("a")).isEqualTo("b");
    }

    @Test
    void shouldDelegateProfileAndPropertyAccess() {
        FactoryLoadedEnvironmentListener.reset();
        StaticApplicationContext context = new StaticApplicationContext();
        ListenableConfigurableEnvironment environment = new ListenableConfigurableEnvironment(
                context.getEnvironment(), context);

        environment.setActiveProfiles("test");
        assertThat(environment.getActiveProfiles()).contains("test");

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("demo", "value");
        environment.getPropertySources().addLast(new MapPropertySource("demo", values));
        assertThat(environment.getProperty("demo")).isEqualTo("value");
        assertThat(FactoryLoadedEnvironmentListener.callbacks()).contains("onPropertySourcesChanged");
    }
}
