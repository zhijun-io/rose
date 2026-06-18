package io.zhijun.spring.core.env;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

import io.zhijun.spring.core.env.event.PropertySourcesChangedEvent;

import static org.assertj.core.api.Assertions.assertThat;

class ListenableMutablePropertySourcesPublishTest {

    private final AtomicReference<PropertySourcesChangedEvent> captured = new AtomicReference<PropertySourcesChangedEvent>();

    private GenericApplicationContext context;

    @BeforeEach
    void setUp() {
        captured.set(null);
        context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) captured::set);
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

        PropertySourcesChangedEvent event = captured.get();
        assertThat(event).isNotNull();
        assertThat(event.getChangedKeys()).contains("demo.key");
    }

    @Test
    void doesNotPublishWhenDisabled() {
        context.close();
        captured.set(null);
        context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("config", Collections.singletonMap(
                        "rose.spring.env.publish-property-source-events", "false")));
        context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) captured::set);
        context.refresh();

        context.getEnvironment().getPropertySources().addLast(
                new MapPropertySource("demo", Collections.singletonMap("demo.key", "v")));

        assertThat(captured.get()).isNull();
    }
}
