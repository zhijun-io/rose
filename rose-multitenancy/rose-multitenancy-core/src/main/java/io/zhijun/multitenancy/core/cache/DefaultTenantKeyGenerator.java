package io.zhijun.multitenancy.core.cache;

import java.lang.reflect.Method;

import org.springframework.lang.Nullable;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import io.zhijun.core.support.Incubating;
import io.zhijun.multitenancy.core.context.TenantContext;

/**
 * An implementation of {@link TenantKeyGenerator} that generates cache keys combining the
 * current tenant identifier with the given method and parameters.
 */
@Incubating
public final class DefaultTenantKeyGenerator implements TenantKeyGenerator {

    @Override
    public Object generate(Object target, Method method, @Nullable Object... params) {
        return SimpleKeyGenerator.generateKey(TenantContext.getRequiredTenantIdentifier(), params);
    }

}
