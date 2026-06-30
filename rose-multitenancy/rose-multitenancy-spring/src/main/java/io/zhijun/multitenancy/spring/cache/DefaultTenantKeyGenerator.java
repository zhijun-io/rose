package io.zhijun.multitenancy.spring.cache;

import io.zhijun.multitenancy.core.context.TenantContext;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.lang.reflect.Method;

/**
 * Generates cache keys combining the current multitenancy identifier with method parameters.
 */

public final class DefaultTenantKeyGenerator implements TenantKeyGenerator {

    @Override
    public Object generate(Object target, Method method, @Nullable Object... params) {
        return SimpleKeyGenerator.generateKey(TenantContext.getRequiredTenantId(), params);
    }
}
