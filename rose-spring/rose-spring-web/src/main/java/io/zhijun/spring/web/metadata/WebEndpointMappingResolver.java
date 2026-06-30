package io.zhijun.spring.web.metadata;

import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * 从 Spring {@link ApplicationContext} 解析 {@link WebEndpointMapping}。
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface WebEndpointMappingResolver {

    /**
     * 从指定 {@link ApplicationContext} 解析出所有 {@link WebEndpointMapping}
     *
     * @param context Spring 应用上下文
     * @return 非空集合
     */
    Collection<WebEndpointMapping> resolve(ApplicationContext context);
}
