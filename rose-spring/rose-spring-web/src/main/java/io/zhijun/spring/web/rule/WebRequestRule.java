package io.zhijun.spring.web.rule;

import org.springframework.web.context.request.NativeWebRequest;

/**
 * Web请求匹配规则接口，受 Spring 的 {@code RequestCondition} 启发。
 * 用于解耦请求匹配逻辑，可在 HandlerMapping 体系之外独立使用。
 */
public interface WebRequestRule {

    /**
     * 判定当前请求是否匹配此规则。
     *
     * @param request 当前请求
     * @return true 表示匹配
     */
    boolean matches(NativeWebRequest request);
}
