package io.zhijun.spring.propertysource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import io.zhijun.spring.env.ListenableConfigurableEnvironmentInitializer;
import io.zhijun.spring.env.PropertySourcesChangedEvent;

/**
 * Verifies {@code propertySources.replace()} on Listenable Environment publishes event with
 * {@code getChangedKeys()} (same path as {@code autoRefreshed} reload).
 */
class PropertySourceAutoRefreshEventTests {

    @Test
    void replaceOnReloadPublishesPropertySourcesChangedEventWithChangedKeys() {
        AtomicReference<PropertySourcesChangedEvent> captured = new AtomicReference<PropertySourcesChangedEvent>();

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) captured::set);
        context.refresh();

        Map<String, Object> original = new HashMap<String, Object>();
        original.put("event.key", "old");
        context.getEnvironment().getPropertySources().addLast(new MapPropertySource("reload-test", original));
        captured.set(null);

        Map<String, Object> updated = new HashMap<String, Object>();
        updated.put("event.key", "new");
        context.getEnvironment()
                .getPropertySources()
                .replace("reload-test", new MapPropertySource("reload-test", updated));

        PropertySourcesChangedEvent event = captured.get();
        assertThat(event).isNotNull();
        assertThat(event.getChangedKeys()).contains("event.key");
        assertThat(context.getEnvironment().getProperty("event.key")).isEqualTo("new");
    }
}
