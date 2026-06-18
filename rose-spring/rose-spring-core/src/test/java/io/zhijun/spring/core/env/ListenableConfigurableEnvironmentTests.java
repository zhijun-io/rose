package io.zhijun.spring.core.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.zhijun.spring.core.env.listener.EnvironmentListener;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.MutablePropertySources;

import static org.assertj.core.api.Assertions.assertThat;

class ListenableConfigurableEnvironmentTests {

    @Test
    void shouldWrapPropertySourcesAndNotifyListeners() {
        StaticApplicationContext context = new StaticApplicationContext();
        List<String> callbacks = new ArrayList<String>();
        EnvironmentListener listener = new EnvironmentListener() {
            @Override
            public void beforeGetPropertySources(ConfigurableEnvironment environment) {
                callbacks.add("before");
            }

            @Override
            public void afterGetPropertySources(ConfigurableEnvironment environment, MutablePropertySources propertySources) {
                callbacks.add("after");
            }
        };

        ListenableConfigurableEnvironment environment = new ListenableConfigurableEnvironment(
                context.getEnvironment(), context, Collections.singletonList(listener));

        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("a", "b");
        propertySources.addLast(new org.springframework.core.env.MapPropertySource("demo", values));

        assertThat(callbacks).containsExactly("before", "after");
        assertThat(environment.containsProperty("a")).isTrue();
    }

    @Test
    void shouldInterceptProfileAndPropertyResolution() {
        FactoryLoadedProfileListener.reset();
        FactoryLoadedPropertyResolverListener.reset();
        StaticApplicationContext context = new StaticApplicationContext();
        List<String> callbacks = new ArrayList<String>();
        EnvironmentListener listener = new EnvironmentListener() {
            @Override
            public void beforeSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
                callbacks.add("set");
            }

            public void beforeGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object defaultValue) {
                callbacks.add("get:" + name);
            }
        };

        ListenableConfigurableEnvironment environment = new ListenableConfigurableEnvironment(
                context.getEnvironment(), context, Collections.singletonList(listener));
        environment.setActiveProfiles("test");
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("demo", "value");
        environment.getPropertySources().addLast(new org.springframework.core.env.MapPropertySource("demo", values));
        assertThat(environment.getProperty("demo")).isEqualTo("value");
        assertThat(callbacks).contains("set", "get:demo");
        assertThat(FactoryLoadedProfileListener.callbacks()).contains("beforeSetActiveProfiles");
        assertThat(FactoryLoadedPropertyResolverListener.callbacks()).contains("beforeGetProperty", "afterGetProperty");
    }
}
