package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public interface ApplicationListenerInterceptorChain {

    void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event);
}
