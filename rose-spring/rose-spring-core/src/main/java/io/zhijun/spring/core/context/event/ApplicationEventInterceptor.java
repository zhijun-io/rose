package io.zhijun.spring.core.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

/**
 * 应用事件拦截器接口，允许在事件传播前后执行预处理、后处理，甚至阻止事件传播。
 *
 * <p>通过 {@link #intercept(ApplicationEvent, ResolvableType, ApplicationEventInterceptorChain)} 
 * 实现切面逻辑，并通过继续或中断链式调用来控制事件传播。</p>
 *
 * <p>通过 {@link Ordered} 或 {@code @Order} 控制拦截器执行顺序。</p>
 */
public interface ApplicationEventInterceptor extends Ordered {

    /**
     * 拦截指定事件。
     *
     * @param event     被拦截的事件
     * @param eventType 事件的解析类型
     * @param chain     拦截器链，用于继续处理
     */
    void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain);

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
