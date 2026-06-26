package io.zhijun.devservice.core.api.config;

import io.zhijun.annotation.Incubating;

/**
 * Default credentials shared by Rose Dev Service connectors.
 */
@Incubating
public final class DevServiceCredentials {

    public static final String DEFAULT_USERNAME = "rose";

    public static final String DEFAULT_PASSWORD = "rose";

    public static final String DEFAULT_DB_NAME = "rose";

    private DevServiceCredentials() {
    }

}
