package io.zhijun.spring.web.metadata;

import java.util.Collection;

/**
 * {@link WebEndpointMapping} 注册中心接口。
 *
 * @since 1.0.0
 */
public interface WebEndpointMappingRegistry {

    /**
     * 注册一个 {@link WebEndpointMapping}
     *
     * @param mapping 映射信息
     * @return 成功注册返回 {@code true}，否则返回 {@code false}
     */
    boolean register(WebEndpointMapping mapping);

    /**
     * 获取所有已注册的 {@link WebEndpointMapping}
     *
     * @return 非空集合
     */
    Collection<WebEndpointMapping> getAll();
}
