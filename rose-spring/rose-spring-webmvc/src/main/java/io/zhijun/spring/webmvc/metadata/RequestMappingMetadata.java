package io.zhijun.spring.webmvc.metadata;

import io.zhijun.spring.web.metadata.HandlerMethodMetadata;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * {@link RequestMappingInfo} 与单个 {@link HandlerMethod} 关联的元数据。
 *
 * @since 1.0.0
 */
public class RequestMappingMetadata extends HandlerMethodMetadata<RequestMappingInfo> {

    public RequestMappingMetadata(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        super(handlerMethod, requestMappingInfo);
    }

    public final RequestMappingInfo getRequestMappingInfo() {
        return getMetadata();
    }
}
