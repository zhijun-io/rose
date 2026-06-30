package io.zhijun.spring.core.env.refresh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import io.zhijun.spring.core.env.ListenableConfigurableEnvironmentInitializer;

class RefreshableContextHolderLifecycleTests {

    @Test
    void bindsOnInitialize() {
        GenericApplicationContext context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        context.refresh();

        assertThat(RefreshableContextHolder.getApplicationContext()).isSameAs(context);
    }
}
