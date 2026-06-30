package io.zhijun.spring.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConfigurableApplicationContextInitializerTests {

    private final MockEnvironment environment = new MockEnvironment();

    private final ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);

    private final ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

    @BeforeEach
    void setUp() {
        reset(context);
        when(context.getEnvironment()).thenReturn(environment);
        when(context.getBeanFactory()).thenReturn(beanFactory);
        when(beanFactory.containsSingleton(anyString())).thenReturn(false);
    }

    @Test
    void shouldInitializeWhenEnabledByDefault() {
        TestInitializer initializer = new TestInitializer();
        initializer.initialize(context);
        assertThat(initializer.initialized).isTrue();
        verify(beanFactory).registerSingleton(TestInitializer.class.getName(), initializer);
    }

    @Test
    void shouldSkipWhenDisabledViaProperty() {
        environment.setProperty("test.initializer.enabled", "false");
        TestInitializer initializer = new TestInitializer("test.initializer.enabled");
        initializer.initialize(context);
        assertThat(initializer.initialized).isFalse();
    }

    @Test
    void shouldUseCustomDefaultEnabled() {
        TestInitializer initializer = new TestInitializer("test.initializer.enabled") {
            @Override
            public boolean getDefaultEnabled() {
                return false;
            }
        };
        initializer.initialize(context);
        assertThat(initializer.initialized).isFalse();

        environment.setProperty("test.initializer.enabled", "true");
        initializer.initialize(context);
        assertThat(initializer.initialized).isTrue();
    }

    @Test
    void shouldRegisterSelfAsSingleton() {
        TestInitializer initializer = new TestInitializer();
        initializer.initialize(context);
        verify(beanFactory).registerSingleton(TestInitializer.class.getName(), initializer);
    }

    @Test
    void shouldNotReRegisterSelfIfAlreadyPresent() {
        when(beanFactory.containsSingleton(TestInitializer.class.getName())).thenReturn(true);
        TestInitializer initializer = new TestInitializer();
        initializer.initialize(context);
        verify(beanFactory, never()).registerSingleton(anyString(), any());
    }

    static class TestInitializer extends ConfigurableApplicationContextInitializer {
        boolean initialized;

        private String enabledPropertyName;

        TestInitializer() {
        }

        TestInitializer(String enabledPropertyName) {
            this.enabledPropertyName = enabledPropertyName;
        }

        @Override
        public String getEnabledPropertyName() {
            return enabledPropertyName;
        }

        @Override
        protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
            initialized = true;
        }
    }
}
