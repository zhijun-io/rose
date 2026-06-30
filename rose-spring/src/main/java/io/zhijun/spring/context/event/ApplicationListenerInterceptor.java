package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.util.function.BiConsumer;

/**
 * Intercepts {@link ApplicationListener} invocations for pre-processing,
 * post-processing, or short-circuiting listener execution.
 * <p>
 * Implementations are ordered via {@link Ordered} or {@code @Order}.
 * <p>
 * (借鉴 microsphere-spring {@code ApplicationListenerInterceptor})
 */
@FunctionalInterface
public interface ApplicationListenerInterceptor extends Ordered {

    /**
     * Intercept the listener invocation. Call {@code chain.accept(listener, event)}
     * to proceed to the next interceptor, or skip to short-circuit propagation.
     */
    void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event,
                   BiConsumer<ApplicationListener<?>, ApplicationEvent> chain);

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
