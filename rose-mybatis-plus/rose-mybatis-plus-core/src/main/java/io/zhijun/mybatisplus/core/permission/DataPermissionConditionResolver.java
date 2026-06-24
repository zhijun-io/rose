package io.zhijun.mybatisplus.core.permission;

import net.sf.jsqlparser.expression.Expression;

/**
 * Converts mapper metadata and principal into a SQL permission expression.
 */
@FunctionalInterface
public interface DataPermissionConditionResolver {

    Expression resolve(DataPermission dataPermission, DataPermissionPrincipal principal);
}
