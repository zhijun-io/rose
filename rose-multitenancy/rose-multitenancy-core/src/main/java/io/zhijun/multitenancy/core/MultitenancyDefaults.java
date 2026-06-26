package io.zhijun.multitenancy.core;

import io.zhijun.annotation.Incubating;

/**
 * Default values shared by multitenancy connectors.
 */
@Incubating
public final class MultitenancyDefaults {

    public static final String DEFAULT_HTTP_HEADER_NAME = "X-TenantId";

    public static final String DEFAULT_HTTP_COOKIE_NAME = "TENANT-ID";

    public static final String DEFAULT_MDC_TENANT_KEY = "tenantId";

    private MultitenancyDefaults() {
    }

}
