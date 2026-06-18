package io.zhijun.spring.core.env.refresh;

import io.zhijun.spring.core.env.ListenableConfigurableEnvironmentInitializer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshableContextHolderTest {

    @BeforeEach
    @AfterEach
    void resetHolder() {
        RefreshableContextHolder.clear();
    }

    @Test
    void bindThenGetReturnsContext() {
        StaticApplicationContext context = new StaticApplicationContext();
        RefreshableContextHolder.bind(context);

        assertThat(RefreshableContextHolder.getApplicationContext()).isSameAs(context);
    }

    @Test
    void getWithoutBindThrows() {
        assertThatThrownBy(RefreshableContextHolder::getApplicationContext)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ApplicationContext not bound");
    }

    @Test
    void childContextDoesNotReplaceRootBinding() {
        GenericApplicationContext parent = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(parent);
        parent.refresh();

        GenericApplicationContext child = new GenericApplicationContext(parent);
        new ListenableConfigurableEnvironmentInitializer().initialize(child);
        child.refresh();

        assertThat(RefreshableContextHolder.getApplicationContext()).isSameAs(parent);
    }
}
