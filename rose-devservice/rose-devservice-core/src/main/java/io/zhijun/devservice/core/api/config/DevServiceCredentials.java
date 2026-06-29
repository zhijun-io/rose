package io.zhijun.devservice.core.api.config;

import org.apiguardian.api.API;

/**
 * Default credentials shared by Rose Dev Service connectors.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class DevServiceCredentials {

    public static final String DEFAULT_USERNAME = "rose";

    public static final String DEFAULT_PASSWORD = "rose";

    public static final String DEFAULT_DB_NAME = "rose";

    private DevServiceCredentials() {}
}
