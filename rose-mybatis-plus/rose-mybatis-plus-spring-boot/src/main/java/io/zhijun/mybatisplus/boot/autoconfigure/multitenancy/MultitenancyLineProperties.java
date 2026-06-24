package io.zhijun.mybatisplus.boot.autoconfigure.multitenancy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for MyBatis-Plus multitenancy line filtering.
 */
@ConfigurationProperties(prefix = MultitenancyLineProperties.CONFIG_PREFIX)
public class MultitenancyLineProperties {

    public static final String CONFIG_PREFIX = "rose.mybatis-plus.multitenancy";

    /**
     * Whether multitenancy line filtering is enabled.
     */
    private boolean enabled = true;

    /**
     * Name of the multitenancy identifier column.
     */
    private String column = "tenant_id";

    /**
     * Table names excluded from multitenancy line filtering.
     */
    private Set<String> ignoreTables = new HashSet<String>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Set<String> getIgnoreTables() {
        return ignoreTables;
    }

    public void setIgnoreTables(Set<String> ignoreTables) {
        this.ignoreTables = ignoreTables != null ? ignoreTables : Collections.<String>emptySet();
    }

}
