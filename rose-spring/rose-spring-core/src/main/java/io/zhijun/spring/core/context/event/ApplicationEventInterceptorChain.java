package io.zhijun.spring.core.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

/**
 * {@link ApplicationEventInterceptor} 责任链。
 */
public interface ApplicationEventInterceptorChain {

    /**
     * 调用链中的下一个拦截器，如果当前是最后一个则最终处理事件。
     */
    void intercept(ApplicationEvent event, ResolvableType eventType);
}
