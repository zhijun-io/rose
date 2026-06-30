package io.zhijun.spring.boot.properties;

import io.zhijun.spring.boot.properties.bind.BindListener;
import io.zhijun.spring.boot.properties.bind.ListenableBindHandlerAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.bind.BindHandler;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListenableConfigurationPropertiesBindHandlerAdvisorTests {

    @Test
    void shouldReturnOriginalHandlerWhenNoListeners() {
        BeanFactory bf = mock(BeanFactory.class);
        ObjectProvider<BindListener> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenReturn(Stream.empty());
        when(bf.getBeanProvider(BindListener.class)).thenReturn(provider);

        ListenableConfigurationPropertiesBindHandlerAdvisor advisor =
                new ListenableConfigurationPropertiesBindHandlerAdvisor();
        advisor.setBeanFactory(bf);
        advisor.afterSingletonsInstantiated();

        BindHandler handler = BindHandler.DEFAULT;
        assertThat(advisor.apply(handler)).isSameAs(handler);
    }

    @Test
    void shouldWrapHandlerWhenListenersPresent() {
        BeanFactory bf = mock(BeanFactory.class);
        ObjectProvider<BindListener> provider = mock(ObjectProvider.class);
        BindListener listener = mock(BindListener.class);
        when(provider.orderedStream()).thenReturn(Stream.of(listener));
        when(bf.getBeanProvider(BindListener.class)).thenReturn(provider);

        ListenableConfigurationPropertiesBindHandlerAdvisor advisor =
                new ListenableConfigurationPropertiesBindHandlerAdvisor();
        advisor.setBeanFactory(bf);
        advisor.afterSingletonsInstantiated();

        BindHandler result = advisor.apply(BindHandler.DEFAULT);
        assertThat(result).isInstanceOf(ListenableBindHandlerAdapter.class);
    }
}
