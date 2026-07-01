package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptingApplicationEventMulticasterTests {

    @Test
    void resolveEventTypeReturnsGivenType() {
        ApplicationEvent event = new ApplicationEvent("source") {};
        ResolvableType type = ResolvableType.forClass(String.class);
        assertThat(InterceptingApplicationEventMulticaster.resolveEventType(event, type)).isSameAs(type);
    }

    @Test
    void resolveEventTypeDerivesFromEventWhenNull() {
        ApplicationEvent event = new ApplicationEvent("source") {};
        ResolvableType result = InterceptingApplicationEventMulticaster.resolveEventType(event, null);
        assertThat(result).isNotNull();
    }

    @Test
    void doInterceptExecutesChainInOrder() {
        List<String> order = new ArrayList<>();
        ApplicationEvent event = new ApplicationEvent("source") {};
        ResolvableType type = ResolvableType.forInstance(event);

        List<ApplicationEventInterceptor> interceptors = Arrays.asList(
            (e, t, chain) -> { order.add("before-1"); chain.accept(e, t); order.add("after-1"); },
            (e, t, chain) -> { order.add("before-2"); chain.accept(e, t); order.add("after-2"); }
        );

        BiConsumer<ApplicationEvent, ResolvableType> fallback = (e, t) -> order.add("fallback");
        InterceptingApplicationEventMulticaster.doIntercept(event, type, interceptors.iterator(), fallback);

        assertThat(order).isEqualTo(Arrays.asList("before-1", "before-2", "fallback", "after-2", "after-1"));
    }

    @Test
    void doInterceptShortCircuitsWhenChainNotCalled() {
        List<String> order = new ArrayList<>();
        ApplicationEvent event = new ApplicationEvent("source") {};
        ResolvableType type = ResolvableType.forInstance(event);

        List<ApplicationEventInterceptor> interceptors = Collections.singletonList(
            (e, t, chain) -> order.add("intercepted-only")
        );

        BiConsumer<ApplicationEvent, ResolvableType> fallback = (e, t) -> order.add("fallback");
        InterceptingApplicationEventMulticaster.doIntercept(event, type, interceptors.iterator(), fallback);

        assertThat(order).isEqualTo(Collections.singletonList("intercepted-only"));
    }

    @Test
    void doInterceptFallsBackWhenNoInterceptors() {
        List<String> order = new ArrayList<>();
        ApplicationEvent event = new ApplicationEvent("source") {};
        ResolvableType type = ResolvableType.forInstance(event);

        BiConsumer<ApplicationEvent, ResolvableType> fallback = (e, t) -> order.add("fallback");
        InterceptingApplicationEventMulticaster.doIntercept(event, type, Collections.<ApplicationEventInterceptor>emptyIterator(), fallback);

        assertThat(order).isEqualTo(Collections.singletonList("fallback"));
    }
}
