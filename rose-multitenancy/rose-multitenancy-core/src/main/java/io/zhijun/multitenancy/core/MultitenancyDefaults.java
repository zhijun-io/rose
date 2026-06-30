package io.zhijun.multitenancy.core;



/**
 * Default values shared by multitenancy connectors.
 */

public final class MultitenancyDefaults {

    public static final String DEFAULT_HTTP_HEADER_NAME = "X-TenantId";

    public static final String DEFAULT_HTTP_COOKIE_NAME = "TENANT-ID";

    public static final String DEFAULT_MDC_TENANT_KEY = "tenantId";

    private MultitenancyDefaults() {}
}
