package io.zhijun.multitenancy.spring.cache;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.lang.Nullable;

import org.apiguardian.api.API;
import io.zhijun.multitenancy.core.context.TenantContext;

/**
 * Generates cache keys combining the current multitenancy identifier with method parameters.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class DefaultTenantKeyGenerator implements TenantKeyGenerator {

    @Override
    public Object generate(Object target, Method method, @Nullable Object... params) {
        return SimpleKeyGenerator.generateKey(TenantContext.getRequiredTenantId(), params);
    }
}
