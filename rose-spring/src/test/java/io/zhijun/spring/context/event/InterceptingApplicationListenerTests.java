package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptingApplicationListenerTests {

    @Test
    void onApplicationEventExecutesInterceptorChain() {
        List<String> order = new ArrayList<>();
        ApplicationEvent event = new ApplicationEvent("source") {};

        @SuppressWarnings("unchecked")
        ApplicationListener<ApplicationEvent> delegate = e -> order.add("delegate");

        List<ApplicationListenerInterceptor> interceptors = Arrays.asList(
            (l, e, chain) -> { order.add("before-1"); chain.accept(l, e); order.add("after-1"); },
            (l, e, chain) -> { order.add("before-2"); chain.accept(l, e); order.add("after-2"); }
        );

        InterceptingApplicationListener listener = new InterceptingApplicationListener(delegate, interceptors);
        listener.onApplicationEvent(event);

        assertThat(order).isEqualTo(Arrays.asList("before-1", "before-2", "delegate", "after-2", "after-1"));
    }

    @Test
    void onApplicationEventShortCircuits() {
        List<String> order = new ArrayList<>();
        ApplicationEvent event = new ApplicationEvent("source") {};

        @SuppressWarnings("unchecked")
        ApplicationListener<ApplicationEvent> delegate = e -> order.add("delegate");

        List<ApplicationListenerInterceptor> interceptors = Collections.singletonList(
            (l, e, chain) -> order.add("intercepted-only")
        );

        InterceptingApplicationListener listener = new InterceptingApplicationListener(delegate, interceptors);
        listener.onApplicationEvent(event);

        assertThat(order).isEqualTo(Collections.singletonList("intercepted-only"));
    }

    @Test
    void onApplicationEventCallsDelegateWhenNoInterceptors() {
        List<String> order = new ArrayList<>();
        ApplicationEvent event = new ApplicationEvent("source") {};

        @SuppressWarnings("unchecked")
        ApplicationListener<ApplicationEvent> delegate = e -> order.add("delegate");

        InterceptingApplicationListener listener = new InterceptingApplicationListener(delegate, Collections.emptyList());
        listener.onApplicationEvent(event);

        assertThat(order).isEqualTo(Collections.singletonList("delegate"));
    }

    @Test
    void getDelegateUnwrapsNestedInterceptingListeners() {
        @SuppressWarnings("unchecked")
        ApplicationListener<ApplicationEvent> original = e -> {};
        InterceptingApplicationListener inner = new InterceptingApplicationListener(original, Collections.emptyList());
        InterceptingApplicationListener outer = new InterceptingApplicationListener(inner, Collections.emptyList());

        assertThat(outer.getDelegate()).isSameAs(original);
    }

    @Test
    void equalsBasedOnDelegate() {
        @SuppressWarnings("unchecked")
        ApplicationListener<ApplicationEvent> delegate = e -> {};
        InterceptingApplicationListener l1 = new InterceptingApplicationListener(delegate, Collections.emptyList());
        InterceptingApplicationListener l2 = new InterceptingApplicationListener(delegate, Collections.emptyList());

        assertThat(l2).isEqualTo(l1);
        assertThat(l2.hashCode()).isEqualTo(l1.hashCode());
    }
}
