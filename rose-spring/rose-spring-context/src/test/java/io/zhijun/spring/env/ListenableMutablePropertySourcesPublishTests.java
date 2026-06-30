package io.zhijun.spring.env;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

import io.zhijun.spring.env.PropertySourcesChangedEvent;

class ListenableMutablePropertySourcesPublishTests {

    private final List<PropertySourcesChangedEvent> capturedEvents = new ArrayList<PropertySourcesChangedEvent>();

    private GenericApplicationContext context;

    @BeforeEach
    void setUp() {
        capturedEvents.clear();
        context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) capturedEvents::add);
        context.refresh();
    }

    @Test
    void publishesOnReplace() {
        Map<String, Object> first = new HashMap<String, Object>();
        first.put("demo.key", "v1");
        Map<String, Object> second = new HashMap<String, Object>();
        second.put("demo.key", "v2");

        context.getEnvironment().getPropertySources().addLast(new MapPropertySource("demo", first));
        context.getEnvironment().getPropertySources().replace("demo", new MapPropertySource("demo", second));

        PropertySourcesChangedEvent event = capturedEvents.get(capturedEvents.size() - 1);
        assertThat(event).isNotNull();
        assertThat(event.getChangedKeys()).contains("demo.key");
    }

    @Test
    void publishesSingleSpringEventOnReplaceOnly() {
        Map<String, Object> first = new HashMap<String, Object>();
        first.put("demo.key", "v1");
        Map<String, Object> second = new HashMap<String, Object>();
        second.put("demo.key", "v2");

        context.getEnvironment().getPropertySources().addLast(new MapPropertySource("demo", first));
        capturedEvents.clear();

        context.getEnvironment().getPropertySources().replace("demo", new MapPropertySource("demo", second));

        assertThat(capturedEvents).hasSize(1);
        assertThat(capturedEvents.get(0).getChangedKeys()).containsExactly("demo.key");
    }

    @Test
    void doesNotPublishWhenDisabled() {
        context.close();
        capturedEvents.clear();
        context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        context.getEnvironment()
                .getPropertySources()
                .addFirst(new MapPropertySource(
                        "config", Collections.singletonMap("rose.spring.env.publish-property-source-event", "false")));
        context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) capturedEvents::add);
        context.refresh();

        context.getEnvironment()
                .getPropertySources()
                .addLast(new MapPropertySource("demo", Collections.singletonMap("demo.key", "v")));

        assertThat(capturedEvents).isEmpty();
    }
}
