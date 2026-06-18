package io.zhijun.spring.core.env.refresh;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Static holder for {@link ApplicationContext}, used by {@link Refreshable} factories instances.
 */
public final class RefreshableContextHolder {

    private static volatile ApplicationContext applicationContext;

    private RefreshableContextHolder() {
    }

    /**
     * Binds the root application context only ({@code getParent() == null}).
     */
    public static void bind(ApplicationContext context) {
        if (context == null || context.getParent() != null) {
            return;
        }
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        ApplicationContext ctx = applicationContext;
        if (ctx == null) {
            throw new IllegalStateException(
                    "ApplicationContext not bound; ensure ListenableConfigurableEnvironmentInitializer ran");
        }
        return ctx;
    }

    public static ApplicationContext peekApplicationContext() {
        return applicationContext;
    }

    public static void clear() {
        applicationContext = null;
    }
}
