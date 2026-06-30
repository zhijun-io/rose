package io.zhijun.spring.core.env;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class EnvRefreshPropertiesTests {

    @Test
    void isPublishPropertySourceEventsReturnsTrueWhenEnvironmentIsNull() {
        assertThat(EnvRefreshProperties.isPublishPropertySourceEvents(null)).isTrue();
    }

    @Test
    void isPublishPropertySourceEventsDefaultsToTrue() {
        MockEnvironment environment = new MockEnvironment();

        assertThat(EnvRefreshProperties.isPublishPropertySourceEvents(environment))
                .isTrue();
    }

    @Test
    void isPublishPropertySourceEventsReadsConfiguredValue() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(EnvRefreshProperties.PUBLISH_EVENTS, "false");

        assertThat(EnvRefreshProperties.isPublishPropertySourceEvents(environment))
                .isFalse();
    }

    @Test
    void isRefreshEnabledDefaultsToTrue() {
        MockEnvironment environment = new MockEnvironment();

        assertThat(EnvRefreshProperties.isRefreshEnabled(environment)).isTrue();
    }

    @Test
    void isRefreshEnabledReadsConfiguredValue() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(EnvRefreshProperties.REFRESH_ENABLED, "false");

        assertThat(EnvRefreshProperties.isRefreshEnabled(environment)).isFalse();
    }
}
