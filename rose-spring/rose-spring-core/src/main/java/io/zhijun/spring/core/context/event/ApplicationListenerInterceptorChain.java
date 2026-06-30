package io.zhijun.spring.core.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * {@link ApplicationListenerInterceptor} 责任链。
 */
public interface ApplicationListenerInterceptorChain {

    /**
     * 调用链中的下一个拦截器，或最终将事件分发给目标监听器。
     */
    void intercept(ApplicationListener<?> listener, ApplicationEvent event);
}
