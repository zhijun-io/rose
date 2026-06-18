package io.zhijun.spring.core.env.refresh;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import io.zhijun.spring.core.env.ListenableConfigurableEnvironmentInitializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshableContextHolderLifecycleTest {

    @Test
    void clearsHolderWhenBoundContextCloses() {
        GenericApplicationContext context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        context.refresh();

        assertThat(RefreshableContextHolder.getApplicationContext()).isSameAs(context);

        context.close();

        assertThatThrownBy(RefreshableContextHolder::getApplicationContext)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ApplicationContext not bound");
    }
}
