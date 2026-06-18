package io.zhijun.spring.core.env.refresh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;

import io.zhijun.spring.core.env.event.PropertySourceChangedEvent;
import io.zhijun.spring.core.env.event.PropertySourcesChangedEvent;

import static org.assertj.core.api.Assertions.assertThat;

class PropertySourcesRefreshOrchestratorTest {

    @AfterEach
    void tearDown() {
        RefreshableContextHolder.clear();
    }

    @Test
    void refreshesWhenPrefixMatches() {
        AtomicInteger count = new AtomicInteger();
        Refreshable refreshable = new Refreshable() {
            @Override
            public boolean supports(Set<String> keys) {
                return keys.stream().anyMatch(key -> key.startsWith("app.messages."));
            }

            @Override
            public void refresh(Set<String> keys) {
                count.incrementAndGet();
            }
        };
        PropertySourcesRefreshOrchestrator orchestrator = new PropertySourcesRefreshOrchestrator(
                Collections.singletonList(refreshable));

        StaticApplicationContext context = new StaticApplicationContext();
        RefreshableContextHolder.bind(context);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("app.messages.welcome", "hi");
        PropertySourcesChangedEvent event = new PropertySourcesChangedEvent(context, Collections.singletonList(
                PropertySourceChangedEvent.replaced(context, new MapPropertySource("demo", props),
                        new MapPropertySource("demo", Collections.emptyMap()))));

        orchestrator.onPropertySourcesChanged(event);

        assertThat(count).hasValue(1);
    }

    @Test
    void skipsRefreshWhenPrefixDoesNotMatch() {
        AtomicInteger count = new AtomicInteger();
        Refreshable refreshable = new Refreshable() {
            @Override
            public boolean supports(Set<String> keys) {
                return keys.stream().anyMatch(key -> key.startsWith("app.messages."));
            }

            @Override
            public void refresh(Set<String> keys) {
                count.incrementAndGet();
            }
        };
        PropertySourcesRefreshOrchestrator orchestrator = new PropertySourcesRefreshOrchestrator(
                Collections.singletonList(refreshable));

        StaticApplicationContext context = new StaticApplicationContext();
        RefreshableContextHolder.bind(context);
        PropertySourcesChangedEvent event = new PropertySourcesChangedEvent(context, Collections.singletonList(
                PropertySourceChangedEvent.added(context,
                        new MapPropertySource("demo", Collections.singletonMap("server.port", "8080")))));

        orchestrator.onPropertySourcesChanged(event);

        assertThat(count).hasValue(0);
    }
}
