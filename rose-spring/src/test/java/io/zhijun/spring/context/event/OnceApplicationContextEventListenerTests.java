package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OnceApplicationContextEventListenerTests {

    @Test
    void shouldHandleEventFromOwnContext() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        TestEventListener listener = new TestEventListener();
        listener.setApplicationContext(ctx);

        ContextRefreshedEvent event = new ContextRefreshedEvent(ctx);
        listener.onApplicationEvent(event);

        assertThat(listener.handled).isTrue();
    }

    @Test
    void shouldSkipEventFromOtherContext() {
        ApplicationContext own = mock(ApplicationContext.class);
        ApplicationContext other = mock(ApplicationContext.class);
        TestEventListener listener = new TestEventListener();
        listener.setApplicationContext(own);

        ContextRefreshedEvent event = new ContextRefreshedEvent(other);
        listener.onApplicationEvent(event);

        assertThat(listener.handled).isFalse();
    }

    @Test
    void shouldThrowWhenContextNotSet() {
        TestEventListener listener = new TestEventListener();
        assertThatThrownBy(listener::getApplicationContext)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ApplicationContext");
    }

    static class TestEventListener extends OnceApplicationContextEventListener<ContextRefreshedEvent> {
        boolean handled;

        @Override
        protected void onApplicationContextEvent(ContextRefreshedEvent event) {
            handled = true;
        }
    }
}
