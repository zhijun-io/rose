package io.zhijun.spring.core.env.refresh;

import org.springframework.context.ApplicationContext;

/**
 * Static holder for {@link ApplicationContext}, used by {@link Refreshable} factories instances.
 */
public final class RefreshableContextHolder {

    private static volatile ApplicationContext applicationContext;

    private RefreshableContextHolder() {
    }

    public static void bind(ApplicationContext context) {
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

    static void clear() {
        applicationContext = null;
    }
}
