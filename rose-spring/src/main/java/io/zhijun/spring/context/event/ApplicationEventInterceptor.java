package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

import java.util.function.BiConsumer;

/**
 * Intercepts {@link ApplicationEvent application events} for pre-processing,
 * post-processing, or short-circuiting event propagation through the
 * continuation chain.
 * <p>
 * Implementations are ordered via {@link Ordered} or {@code @Order}.
 * <p>
 * (借鉴 microsphere-spring {@code ApplicationEventInterceptor})
 *
 * @see Ordered
 */
@FunctionalInterface
public interface ApplicationEventInterceptor extends Ordered {

    /**
     * Intercept the event. Call {@code chain.accept(event, eventType)} to proceed
     * to the next interceptor, or skip to short-circuit event propagation.
     */
    void intercept(ApplicationEvent event, ResolvableType eventType, BiConsumer<ApplicationEvent, ResolvableType> chain);

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
