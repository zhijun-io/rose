package io.zhijun.spring.boot.properties.bind;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ListenableBindHandlerAdapterTests {

    private final ConfigurationPropertyName name = ConfigurationPropertyName.of("test");

    private final Bindable<String> target = Bindable.of(String.class);

    private final BindContext context = mock(BindContext.class);

    @Test
    void shouldDelegateOnStartToListeners() {
        BindListener listener = mock(BindListener.class);
        ListenableBindHandlerAdapter adapter = new ListenableBindHandlerAdapter(BindHandler.DEFAULT, Collections.singletonList(listener));
        adapter.onStart(name, target, context);
        verify(listener).onStart(name, target, context);
    }

    @Test
    void shouldDelegateOnSuccessToListeners() {
        BindListener listener = mock(BindListener.class);
        when(listener.onSuccess(name, target, context, "result")).thenReturn("modified");
        ListenableBindHandlerAdapter adapter = new ListenableBindHandlerAdapter(BindHandler.DEFAULT, Collections.singletonList(listener));
        Object result = adapter.onSuccess(name, target, context, "result");
        assertThat(result).isEqualTo("modified");
    }

    @Test
    void shouldDelegateOnFailureToListeners() throws Exception {
        BindListener listener = mock(BindListener.class);
        Exception error = new RuntimeException("test error");
        BindHandler parent = mock(BindHandler.class);
        doThrow(error).when(parent).onFailure(any(), any(), any(), any());
        ListenableBindHandlerAdapter adapter = new ListenableBindHandlerAdapter(parent, Collections.singletonList(listener));
        assertThatThrownBy(() -> adapter.onFailure(name, target, context, error))
                .isInstanceOf(RuntimeException.class);
        verify(listener).onFailure(name, target, context, error);
    }

    @Test
    void shouldDelegateOnFinishToListeners() throws Exception {
        BindListener listener = mock(BindListener.class);
        ListenableBindHandlerAdapter adapter = new ListenableBindHandlerAdapter(BindHandler.DEFAULT, Collections.singletonList(listener));
        adapter.onFinish(name, target, context, "result");
        verify(listener).onFinish(name, target, context, "result");
    }

    @Test
    void shouldChainMultipleListeners() {
        BindListener l1 = mock(BindListener.class);
        BindListener l2 = mock(BindListener.class);
        ListenableBindHandlerAdapter adapter = new ListenableBindHandlerAdapter(BindHandler.DEFAULT, java.util.Arrays.asList(l1, l2));
        adapter.onStart(name, target, context);
        verify(l1).onStart(name, target, context);
        verify(l2).onStart(name, target, context);
    }
}
