package io.zhijun.mybatisplus.core.permission;

/**
 * Resolves the current principal for data permission filtering.
 */
@FunctionalInterface
public interface DataPermissionPrincipalResolver {

    DataPermissionPrincipal resolve();
}
