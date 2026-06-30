package io.zhijun.spring.context.event;

import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 注册 {@link EventPublishingBeanBeforeProcessor}，启用 bean 生命周期事件发布。
 * <p>
 * 可通过配置属性 {@value #ENABLED_PROPERTY_NAME} 启用/禁用（默认启用）。
 * <p>
 * （借鉴 microsphere-spring {@code EventPublishingBeanInitializer}）
 *
 * @see EventPublishingBeanBeforeProcessor
 * @see EventPublishingBeanAfterProcessor
 * @see BeanListeners
 * @see BeanListener
 * @see ApplicationContextInitializer
 * @see PriorityOrdered
 */
public class EventPublishingBeanInitializer extends ConfigurableApplicationContextInitializer implements PriorityOrdered {

    public static final String PROPERTY_KEY_PREFIX = "rose.spring.event-publishing-bean.";

    public static final String ENABLED_PROPERTY_NAME = PROPERTY_KEY_PREFIX + "enabled";

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        context.addBeanFactoryPostProcessor(new EventPublishingBeanBeforeProcessor());
    }

    @Override
    protected boolean isEnabled(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        return environment.getProperty(ENABLED_PROPERTY_NAME, boolean.class, true);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
