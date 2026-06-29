package io.zhijun.spring.boot.bind;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class RoseBinderTests {

    @Test
    void shouldBindBooleanWithDefaultValue() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("rose.test.enabled", "true");

        RoseBinder binder = RoseBinder.get(environment);

        assertThat(binder.bindBoolean("rose.test.enabled", false)).isTrue();
        assertThat(binder.bindBoolean("rose.test.missing", false)).isFalse();
    }

    @Test
    void shouldBindStringWithDefaultValue() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("rose.test.name", "rose");

        RoseBinder binder = RoseBinder.get(environment);

        assertThat(binder.bindString("rose.test.name", null)).isEqualTo("rose");
        assertThat(binder.bindString("rose.test.missing", "default")).isEqualTo("default");
    }
}
