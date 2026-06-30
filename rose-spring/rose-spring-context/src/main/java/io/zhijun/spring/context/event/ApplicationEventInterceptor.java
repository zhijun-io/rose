package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

/**
 * Intercepts {@link ApplicationEvent application events} for pre-processing,
 * post-processing, or short-circuiting event propagation through the
 * {@link ApplicationEventInterceptorChain}.
 * <p>
 * Implementations are ordered via {@link Ordered} or {@code @Order}.
 * <p>
 * (借鉴 microsphere-spring {@code ApplicationEventInterceptor})
 *
 * @see ApplicationEventInterceptorChain
 * @see Ordered
 */
public interface ApplicationEventInterceptor extends Ordered {

    /**
     * Intercept the event. Call {@code chain.intercept(event, eventType)} to proceed,
     * or skip to short-circuit event propagation.
     */
    void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain);

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
