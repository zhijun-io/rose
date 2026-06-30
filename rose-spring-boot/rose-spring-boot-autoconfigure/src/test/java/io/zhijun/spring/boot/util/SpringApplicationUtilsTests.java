package io.zhijun.spring.boot.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SpringApplicationUtilsTests {

    @Test
    void shouldAddDefaultPropertiesResource() {
        Set<String> before = SpringApplicationUtils.getDefaultPropertiesResources();
        int size = before.size();
        SpringApplicationUtils.addDefaultPropertiesResource("classpath:custom.properties");
        assertThat(SpringApplicationUtils.getDefaultPropertiesResources()).hasSize(size + 1);
    }

    @Test
    void shouldAddDefaultPropertiesResourcesVarargs() {
        Set<String> before = SpringApplicationUtils.getDefaultPropertiesResources();
        int size = before.size();
        SpringApplicationUtils.addDefaultPropertiesResources("classpath:a.properties", "classpath:b.properties");
        assertThat(SpringApplicationUtils.getDefaultPropertiesResources()).hasSize(size + 2);
    }

    @Test
    void shouldNotAddEmptyResource() {
        Set<String> before = SpringApplicationUtils.getDefaultPropertiesResources();
        SpringApplicationUtils.addDefaultPropertiesResource("");
        assertThat(SpringApplicationUtils.getDefaultPropertiesResources()).hasSameSizeAs(before);
    }

    @Test
    void shouldReturnDefaultLoggingLevelWhenNoProperty() {
        assertThat(SpringApplicationUtils.getLoggingLevel((MockEnvironment) null)).isEqualTo("INFO");
    }

    @Test
    void shouldResolveLoggingLevelFromEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("io.zhijun.spring.boot.logging-level", "DEBUG");
        assertThat(SpringApplicationUtils.getLoggingLevel(env)).isEqualTo("DEBUG");
    }

    @Test
    void shouldGetResourceLoaderFromSpringApplication() {
        SpringApplication app = mock(SpringApplication.class);
        when(app.getResourceLoader()).thenReturn(null);
        when(app.getClassLoader()).thenReturn(getClass().getClassLoader());
        assertThat(SpringApplicationUtils.getResourceLoader(app)).isNotNull();
    }
}
