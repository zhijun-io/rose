package io.zhijun.spring.core.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.jspecify.annotations.Nullable;

/**
 * Spring上下文持有器，自动注入ApplicationContext
 * <p>零配置自动注册，无需用户手动声明Bean
 * <p>同时跟踪根上下文用于配置刷新，关闭时自动清理。
 */
public class SpringContextHolder implements ApplicationContextAware, ApplicationListener<ContextClosedEvent> {
    @Nullable
    private static volatile ApplicationContext applicationContext;

    @Nullable
    private static volatile ApplicationContext refreshableContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
        if (applicationContext != null && applicationContext.getParent() == null) {
            refreshableContext = applicationContext;
        }
    }

    /**
     * 获取Spring上下文，非Spring环境下返回null
     */
    @Nullable
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 绑定根上下文，仅接受无父级的上下文（来自 RefreshableContextHolder）。
     */
    public static void bind(ApplicationContext context) {
        if (context == null || context.getParent() != null) {
            return;
        }
        refreshableContext = context;
    }

    /**
     * 获取可刷新的根上下文，未绑定时抛出异常。
     */
    public static ApplicationContext getRefreshableContext() {
        ApplicationContext ctx = refreshableContext;
        if (ctx == null) {
            throw new IllegalStateException(
                    "Refreshable ApplicationContext not bound; ensure ListenableConfigurableEnvironmentInitializer ran");
        }
        return ctx;
    }

    @Nullable
    public static ApplicationContext peekRefreshableContext() {
        return refreshableContext;
    }

    public static void clear() {
        refreshableContext = null;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        ApplicationContext bound = refreshableContext;
        if (bound != null && bound == event.getApplicationContext()) {
            refreshableContext = null;
        }
    }

    /**
     * 按类型获取Bean，不存在返回null
     */
    @Nullable
    public static <T> T getBean(Class<T> beanClass) {
        ApplicationContext ctx = applicationContext;
        if (ctx == null) {
            return null;
        }
        try {
            return ctx.getBean(beanClass);
        } catch (BeansException e) {
            return null;
        }
    }

    /**
     * 按名称和类型获取Bean，不存在返回null
     */
    @Nullable
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        ApplicationContext ctx = applicationContext;
        if (ctx == null) {
            return null;
        }
        try {
            return ctx.getBean(beanName, beanClass);
        } catch (BeansException e) {
            return null;
        }
    }

    /**
     * 是否是Spring环境
     */
    public static boolean isSpringEnvironment() {
        return applicationContext != null;
    }
}
