package io.zhijun.data.mybatisplus.permission;

import org.springframework.lang.Nullable;

/**
 * Resolves the current principal for data permission filtering.
 */
@FunctionalInterface
public interface DataPermissionPrincipalResolver {

    @Nullable
    DataPermissionPrincipal resolve();
}
