package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

public interface ApplicationListenerInterceptor extends Ordered {

    void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event, ApplicationListenerInterceptorChain chain);

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
