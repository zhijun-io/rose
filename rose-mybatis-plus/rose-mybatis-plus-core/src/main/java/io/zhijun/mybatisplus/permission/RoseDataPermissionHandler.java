package io.zhijun.mybatisplus.permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;

/**
 * SPI-based data permission handler.
 */
public class RoseDataPermissionHandler implements DataPermissionHandler {

    private static final String DOT = ".";

    private final DataPermissionPrincipalResolver principalResolver;

    private final DataPermissionConditionResolver conditionResolver;

    private final Map<String, DataPermission> annotationCache = new ConcurrentHashMap<String, DataPermission>();

    private final Set<String> excludedMappedStatementIds = new ConcurrentSkipListSet<String>();

    private final Set<String> defaultIgnoredMethods = new HashSet<String>(Arrays.asList(
            "selectById", "selectBatchIds", "selectOne", "deleteById", "deleteByMap", "delete", "deleteBatchIds"));

    public RoseDataPermissionHandler(DataPermissionPrincipalResolver principalResolver,
            DataPermissionConditionResolver conditionResolver) {
        this.principalResolver = principalResolver;
        this.conditionResolver = conditionResolver;
    }

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        if (excludedMappedStatementIds.contains(mappedStatementId)) {
            return where;
        }

        int lastDot = mappedStatementId.lastIndexOf(DOT);
        if (lastDot < 0) {
            return where;
        }

        String method = mappedStatementId.substring(lastDot + 1);
        if (defaultIgnoredMethods.contains(method)) {
            return where;
        }

        String className = mappedStatementId.substring(0, lastDot);
        DataPermission dataPermission = resolveAnnotation(className);
        if (dataPermission == null) {
            return where;
        }

        if (ArrayUtils.isNotEmpty(dataPermission.ignoreMethods())) {
            for (String ignoreMethod : dataPermission.ignoreMethods()) {
                if (Objects.equals(method, ignoreMethod)) {
                    excludedMappedStatementIds.add(mappedStatementId);
                    return where;
                }
            }
        }

        if (StringUtils.isBlank(dataPermission.column())) {
            return where;
        }

        DataPermissionPrincipal principal = principalResolver.resolve();
        if (principal == null || principal.isBypass()) {
            return where;
        }

        Expression permissionExpression = conditionResolver.resolve(dataPermission, principal);
        if (permissionExpression == null) {
            return where;
        }

        if (where == null) {
            return new Parenthesis(permissionExpression);
        }
        return new AndExpression(where, permissionExpression);
    }

    private DataPermission resolveAnnotation(String className) {
        DataPermission cached = annotationCache.get(className);
        if (cached != null) {
            return cached;
        }
        try {
            Class<?> mapperType = Class.forName(className);
            DataPermission annotation = mapperType.getAnnotation(DataPermission.class);
            if (annotation != null) {
                annotationCache.put(className, annotation);
                return annotation;
            }
        } catch (ClassNotFoundException ignored) {
            // mapper interface not on classpath yet
        }
        return null;
    }
}
