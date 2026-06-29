package io.zhijun.multitenancy.core;

import org.apiguardian.api.API;

/**
 * Default values shared by multitenancy connectors.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class MultitenancyDefaults {

    public static final String DEFAULT_HTTP_HEADER_NAME = "X-TenantId";

    public static final String DEFAULT_HTTP_COOKIE_NAME = "TENANT-ID";

    public static final String DEFAULT_MDC_TENANT_KEY = "tenantId";

    private MultitenancyDefaults() {}
}
