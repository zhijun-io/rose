package io.zhijun.spring.core.binder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationBeanBinderTests {

    @Test
    void bindShouldApplySimpleProperties() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        User user = new User();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "rose");
        properties.put("age", "18");

        binder.bind(properties, true, true, user);

        assertThat(user.getName()).isEqualTo("rose");
        assertThat(user.getAge()).isEqualTo(18);
    }

    @Test
    void bindShouldIgnoreUnknownFieldsWhenConfigured() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        User user = new User();

        binder.bind(Collections.<String, Object>singletonMap("missing", "value"), true, true, user);

        assertThat(user.getName()).isNull();
    }

    @Test
    void bindShouldIgnoreInvalidFieldsWhenConfigured() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        User user = new User();

        binder.bind(Collections.<String, Object>singletonMap("age", "oops"), true, true, user);

        assertThat(user.getAge()).isZero();
    }
}
