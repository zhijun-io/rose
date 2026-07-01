package io.zhijun.spring.web.metadata;

import java.util.function.Predicate;

/**
 * {@link WebEndpointMapping} 过滤器接口。
 */
@FunctionalInterface
public interface WebEndpointMappingFilter extends Predicate<WebEndpointMapping> {
}
