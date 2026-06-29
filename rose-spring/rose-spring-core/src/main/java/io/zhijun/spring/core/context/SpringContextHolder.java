package io.zhijun.spring.core.context;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
/**
 * Spring上下文持有器，自动注入ApplicationContext
 * <p>零配置自动注册，无需用户手动声明Bean
 */
public class SpringContextHolder implements ApplicationContextAware {
    @Nullable
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }
    /**
     * 获取Spring上下文，非Spring环境下返回null
     */
    @Nullable
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    /**
     * 按类型获取Bean，不存在返回null
     */
    @Nullable
    public static <T> T getBean(Class<T> beanClass) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(beanClass);
        } catch (BeansException e) {
            return null;
        }
    }
    /**
     * 按名称和类型获取Bean，不存在返回null
     */
    @Nullable
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(beanName, beanClass);
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
