package io.zhijun.spring.core.env.refresh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

import io.zhijun.spring.core.env.ListenableConfigurableEnvironmentInitializer;
import io.zhijun.spring.core.env.event.PropertySourcesChangedEvent;

import static org.assertj.core.api.Assertions.assertThat;

class PropertySourcesRefreshEnvironmentListenerIntegrationTest {

    @BeforeEach
    void setUp() {
        TestRefreshable.reset();
        RefreshableContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        RefreshableContextHolder.clear();
    }

    @Test
    void dispatchesTestRefreshableAfterContextIsActive() {
        GenericApplicationContext context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        context.refresh();

        Map<String, Object> first = new HashMap<String, Object>();
        first.put("integration.key", "v1");
        Map<String, Object> second = new HashMap<String, Object>();
        second.put("integration.key", "v2");
        context.getEnvironment().getPropertySources().addLast(new MapPropertySource("demo", first));
        context.getEnvironment().getPropertySources().replace("demo", new MapPropertySource("demo", second));

        assertThat(TestRefreshable.REFRESH_COUNT.get()).isGreaterThanOrEqualTo(1);
        assertThat(TestRefreshable.LAST_KEYS).contains("integration.key");
    }

    @Test
    void doesNotDispatchBeforeContextIsActive() {
        GenericApplicationContext context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);

        context.getEnvironment().getPropertySources().addLast(
                new MapPropertySource("demo", Collections.singletonMap("integration.key", "v")));

        assertThat(TestRefreshable.REFRESH_COUNT).hasValue(0);
    }
}
