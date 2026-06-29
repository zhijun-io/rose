package io.zhijun.mybatisplus.core.permission;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
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

    /** Class names known to have no @DataPermission or to be unloadable, to avoid repeated reflection. */
    private final Set<String> noAnnotationCache = new ConcurrentSkipListSet<String>();

    private final Set<String> excludedMappedStatementIds = new ConcurrentSkipListSet<String>();

    private final Set<String> defaultIgnoredMethods = new HashSet<String>(Arrays.asList(
            "selectById", "selectBatchIds", "selectOne", "deleteById", "deleteByMap", "delete", "deleteBatchIds"));

    public RoseDataPermissionHandler(
            DataPermissionPrincipalResolver principalResolver, DataPermissionConditionResolver conditionResolver) {
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
        String className = mappedStatementId.substring(0, lastDot);

        if (defaultIgnoredMethods.contains(method) && !declaresOwnMethod(className, method)) {
            excludedMappedStatementIds.add(mappedStatementId);
            return where;
        }

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

    /**
     * Returns {@code true} when the mapper interface declares its own method with the given name,
     * so that custom methods shadowing {@code BaseMapper} defaults are still subject to data
     * permission instead of being silently skipped.
     */
    private boolean declaresOwnMethod(String className, String methodName) {
        try {
            Class<?> mapperType = Class.forName(className);
            for (Method method : mapperType.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return true;
                }
            }
        } catch (ClassNotFoundException ignored) {
            // mapper interface not on classpath yet
        }
        return false;
    }

    private DataPermission resolveAnnotation(String className) {
        DataPermission cached = annotationCache.get(className);
        if (cached != null) {
            return cached;
        }
        if (noAnnotationCache.contains(className)) {
            return null;
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
        noAnnotationCache.add(className);
        return null;
    }
}
