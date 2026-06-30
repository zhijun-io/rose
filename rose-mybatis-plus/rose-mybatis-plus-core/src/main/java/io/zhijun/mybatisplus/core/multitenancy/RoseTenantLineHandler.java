package io.zhijun.mybatisplus.core.multitenancy;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * {@link TenantLineHandler} that resolves the multitenancy identifier from a {@link TenantIdSupplier}.
 */
public final class RoseTenantLineHandler implements TenantLineHandler {

    private final TenantIdSupplier tenantIdSupplier;

    private final String tenantIdColumn;

    private final Set<String> ignoreTables;

    public RoseTenantLineHandler(
            TenantIdSupplier tenantIdSupplier, String tenantIdColumn, Collection<String> ignoreTables) {
        Objects.requireNonNull(tenantIdSupplier, "tenantIdSupplier cannot be null");
        if (StringUtils.isBlank(tenantIdColumn)) {
            throw new IllegalArgumentException("tenantIdColumn cannot be null or empty");
        }
        this.tenantIdSupplier = tenantIdSupplier;
        this.tenantIdColumn = tenantIdColumn;
        this.ignoreTables = Collections.unmodifiableSet(new HashSet<String>(ignoreTables));
    }

    @Override
    public Expression getTenantId() {
        return new StringValue(tenantIdSupplier.getTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return tenantIdColumn;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return ignoreTables.contains(tableName);
    }
}
