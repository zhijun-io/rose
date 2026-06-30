package io.zhijun.spring.core.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * 应用监听器拦截器接口，在监听器处理事件前后提供额外行为。
 */
public interface ApplicationListenerInterceptor extends Ordered {

    /**
     * 拦截指定监听器对事件的处理。
     */
    void intercept(ApplicationListener<?> listener, ApplicationEvent event, ApplicationListenerInterceptorChain chain);

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
