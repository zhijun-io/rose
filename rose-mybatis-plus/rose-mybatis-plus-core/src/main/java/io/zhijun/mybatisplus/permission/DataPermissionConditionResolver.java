package io.zhijun.mybatisplus.permission;

import org.springframework.lang.Nullable;

import net.sf.jsqlparser.expression.Expression;

/**
 * Converts mapper metadata and principal into a SQL permission expression.
 */
@FunctionalInterface
public interface DataPermissionConditionResolver {

    @Nullable
    Expression resolve(DataPermission dataPermission, DataPermissionPrincipal principal);
}
