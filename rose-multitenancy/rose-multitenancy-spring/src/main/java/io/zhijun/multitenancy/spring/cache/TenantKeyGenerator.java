package io.zhijun.multitenancy.spring.cache;

import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Cache key generator for tenant-aware Spring Cache keys.
 */
@FunctionalInterface
public interface TenantKeyGenerator extends KeyGenerator {
}
