package io.zhijun.multitenancy.spring.cache;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.jspecify.annotations.Nullable;


import io.zhijun.multitenancy.core.context.TenantContext;

/**
 * Generates cache keys combining the current multitenancy identifier with method parameters.
 */

public final class DefaultTenantKeyGenerator implements TenantKeyGenerator {

    @Override
    public Object generate(Object target, Method method, @Nullable Object... params) {
        return SimpleKeyGenerator.generateKey(TenantContext.getRequiredTenantId(), params);
    }
}
