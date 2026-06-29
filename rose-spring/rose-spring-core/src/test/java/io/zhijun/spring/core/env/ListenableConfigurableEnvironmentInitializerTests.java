package io.zhijun.spring.core.env;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MutablePropertySources;

import io.zhijun.spring.core.env.refresh.RefreshableContextHolder;

class ListenableConfigurableEnvironmentInitializerTests {

    @Test
    void shouldReplaceEnvironmentWithListenableOneAndLoadFactories() {
        FactoryLoadedEnvironmentListener.reset();
        GenericApplicationContext context = new GenericApplicationContext();
        new ListenableConfigurableEnvironmentInitializer().initialize(context);
        assertThat(context.getEnvironment()).isInstanceOf(ListenableConfigurableEnvironment.class);

        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        propertySources.addLast(new org.springframework.core.env.MapPropertySource(
                "demo", java.util.Collections.singletonMap("a", "b")));

        assertThat(FactoryLoadedEnvironmentListener.callbacks())
                .containsExactly("beforeGetPropertySources", "afterGetPropertySources");
        assertThat(RefreshableContextHolder.getApplicationContext()).isSameAs(context);
    }
}
