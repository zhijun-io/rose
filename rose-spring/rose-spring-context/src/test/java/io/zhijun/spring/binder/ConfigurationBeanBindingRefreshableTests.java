package io.zhijun.spring.binder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import io.zhijun.spring.binder.ConfigurationBeanCustomizer;
import io.zhijun.spring.binder.User;
import io.zhijun.spring.binder.EnableConfigurationBeanBinding;
import io.zhijun.spring.env.ListenableConfigurableEnvironmentInitializer;
import io.zhijun.spring.context.SpringContextHolder;

class ConfigurationBeanBindingRefreshableTests {

    private static final AtomicInteger CUSTOMIZE_COUNT = new AtomicInteger();

    @AfterEach
    void tearDown() {
        CUSTOMIZE_COUNT.set(0);
        SpringContextHolder.clear();
    }

    @Test
    void supportsWhenPrefixKeyChanges() {
        AnnotationConfigApplicationContext context = startContext();
        ConfigurationBeanBindingRefreshable refreshable = new ConfigurationBeanBindingRefreshable();

        assertThat(refreshable.supports(Collections.singleton("usr.age"))).isTrue();
        assertThat(refreshable.supports(Collections.singleton("server.port"))).isFalse();

        context.close();
    }

    @Test
    void rebindsExistingBeanWhenPrefixKeyChanges() {
        AnnotationConfigApplicationContext context = startContext();
        User user = context.getBean("m", User.class);
        assertThat(user.getAge()).isEqualTo(34);

        Map<String, Object> updated = new HashMap<String, Object>();
        updated.put("usr.id", "m");
        updated.put("usr.name", "mercyblitz");
        updated.put("usr.age", "40");
        context.getEnvironment().getPropertySources().replace("test", new MapPropertySource("test", updated));

        new ConfigurationBeanBindingRefreshable().refresh(Collections.singleton("usr.age"));

        assertThat(user.getAge()).isEqualTo(40);
        context.close();
    }

    @Test
    void rebindRunsCustomizersAgain() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(RefreshBindingConfigWithCustomizer.class);
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        Map<String, Object> initial = new HashMap<String, Object>();
        initial.put("usr.id", "m");
        initial.put("usr.name", "mercyblitz");
        initial.put("usr.age", "10");
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", initial));
        context.refresh();

        assertThat(CUSTOMIZE_COUNT.get()).isGreaterThanOrEqualTo(1);
        User user = context.getBean("m", User.class);
        assertThat(user.getAge()).isEqualTo(99);

        int beforeRefresh = CUSTOMIZE_COUNT.get();
        Map<String, Object> updated = new HashMap<String, Object>();
        updated.put("usr.id", "m");
        updated.put("usr.name", "mercyblitz");
        updated.put("usr.age", "11");
        context.getEnvironment().getPropertySources().replace("test", new MapPropertySource("test", updated));
        new ConfigurationBeanBindingRefreshable().refresh(Collections.singleton("usr.name"));

        assertThat(CUSTOMIZE_COUNT.get()).isGreaterThan(beforeRefresh);
        assertThat(user.getAge()).isEqualTo(99);
        context.close();
    }

    private static AnnotationConfigApplicationContext startContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(RefreshBindingConfig.class);
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        Map<String, Object> initial = new HashMap<String, Object>();
        initial.put("usr.id", "m");
        initial.put("usr.name", "mercyblitz");
        initial.put("usr.age", "34");
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", initial));
        context.refresh();
        return context;
    }

    @Configuration
    @EnableConfigurationBeanBinding(prefix = "usr", type = User.class)
    static class RefreshBindingConfig {}

    @Configuration
    @EnableConfigurationBeanBinding(prefix = "usr", type = User.class)
    static class RefreshBindingConfigWithCustomizer {

        @Bean
        ConfigurationBeanCustomizer countingCustomizer() {
            return new ConfigurationBeanCustomizer() {
                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public void customize(String beanName, Object configurationBean) {
                    CUSTOMIZE_COUNT.incrementAndGet();
                    if (configurationBean instanceof User) {
                        ((User) configurationBean).setAge(99);
                    }
                }
            };
        }
    }
}
