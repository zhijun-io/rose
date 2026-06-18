package io.zhijun.spring.core.env.refresh;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Environment property keys for env-refresh behavior.
 */
public final class RoseSpringEnvironmentRefreshProperties {

    public static final String PUBLISH_EVENTS = "rose.spring.env.publish-property-source-events";

    public static final String ORCHESTRATOR_ENABLED = "rose.spring.env.refresh-orchestrator.enabled";

    private RoseSpringEnvironmentRefreshProperties() {
    }

    public static boolean isPublishPropertySourceEvents(ApplicationContext context) {
        if (context == null) {
            return true;
        }
        Environment env = context.getEnvironment();
        return env.getProperty(PUBLISH_EVENTS, Boolean.class, Boolean.TRUE);
    }

    public static boolean isOrchestratorEnabled(Environment environment) {
        return environment.getProperty(ORCHESTRATOR_ENABLED, Boolean.class, Boolean.TRUE);
    }
}
