package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

/**
 * Chain of {@link ApplicationEventInterceptor} instances.
 * <p>
 * Each interceptor decides whether to pass the event to the next via
 * {@link #intercept(ApplicationEvent, ResolvableType)}.
 * <p>
 * (借鉴 microsphere-spring {@code ApplicationEventInterceptorChain})
 *
 * @see ApplicationEventInterceptor
 */
public interface ApplicationEventInterceptorChain {

    /**
     * Proceed to the next interceptor in the chain, or invoke the final event handler.
     */
    void intercept(ApplicationEvent event, ResolvableType eventType);
}
