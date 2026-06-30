package io.zhijun.spring.core.env.refresh;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Environment property keys for env-refresh behavior.
 */
public final class EnvRefreshProperties {

    public static final String PUBLISH_EVENTS = "rose.spring.env.publish-property-source-event";

    public static final String REFRESH_ENABLED = "rose.spring.env.refresh.enabled";

    private EnvRefreshProperties() {}

    public static boolean isPublishPropertySourceEvents(ApplicationContext context) {
        if (context == null) {
            return true;
        }
        Environment env = context.getEnvironment();
        return env.getProperty(PUBLISH_EVENTS, Boolean.class, Boolean.TRUE);
    }

    public static boolean isRefreshEnabled(Environment environment) {
        return environment.getProperty(REFRESH_ENABLED, Boolean.class, Boolean.TRUE);
    }
}
