package io.zhijun.multitenancy.core.context.resolver;

import io.zhijun.core.annotation.Incubating;
import io.zhijun.core.annotation.Nullable;

/**
 * Strategy used to resolve the current tenant from a given source context.
 */
@FunctionalInterface
public interface TenantResolver<T> {

    /**
     * Resolves a tenant identifier from the given source.
     */
    @Nullable
    String resolveTenantId(T source);

}
