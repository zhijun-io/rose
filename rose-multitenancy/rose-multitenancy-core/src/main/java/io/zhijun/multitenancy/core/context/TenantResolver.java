package io.zhijun.multitenancy.core.context;

import io.zhijun.core.annotation.Nullable;

/**
 * Strategy used to resolve the current multitenancy from a given source context.
 */
@FunctionalInterface
public interface TenantResolver<T> {

    /**
     * Resolves a multitenancy identifier from the given source.
     */
    @Nullable
    String resolveTenantId(T source);

}
