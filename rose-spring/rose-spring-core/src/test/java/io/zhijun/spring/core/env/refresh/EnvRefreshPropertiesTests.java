package io.zhijun.spring.core.env.refresh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class EnvRefreshPropertiesTests {

    @Mock
    private ApplicationContext applicationContext;

    @Test
    void isPublishPropertySourceEventsReturnsTrueWhenContextIsNull() {
        assertThat(EnvRefreshProperties.isPublishPropertySourceEvents(null)).isTrue();
    }

    @Test
    void isPublishPropertySourceEventsDefaultsToTrue() {
        MockEnvironment environment = new MockEnvironment();
        when(applicationContext.getEnvironment()).thenReturn(environment);

        assertThat(EnvRefreshProperties.isPublishPropertySourceEvents(applicationContext))
                .isTrue();
    }

    @Test
    void isPublishPropertySourceEventsReadsConfiguredValue() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(EnvRefreshProperties.PUBLISH_EVENTS, "false");
        when(applicationContext.getEnvironment()).thenReturn(environment);

        assertThat(EnvRefreshProperties.isPublishPropertySourceEvents(applicationContext))
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
