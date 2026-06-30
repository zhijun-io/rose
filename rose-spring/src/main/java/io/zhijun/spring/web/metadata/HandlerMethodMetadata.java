package io.zhijun.spring.web.metadata;

import org.springframework.web.method.HandlerMethod;

/**
 * Spring WebMVC {@link HandlerMethod} 的元数据包装。
 *
 * @param <M> 元数据类型
 * @since 1.0.0
 */
public class HandlerMethodMetadata<M> extends HandlerMetadata<HandlerMethod, M> {

    public HandlerMethodMetadata(HandlerMethod handlerMethod, M metadata) {
        super(handlerMethod, metadata);
    }

    public final HandlerMethod getHandlerMethod() {
        return getHandler();
    }
}
