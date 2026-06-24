package io.zhijun.multitenancy.spring.cache;

import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Cache key generator for multitenancy-aware Spring Cache keys.
 */
@FunctionalInterface
public interface TenantKeyGenerator extends KeyGenerator {
}
