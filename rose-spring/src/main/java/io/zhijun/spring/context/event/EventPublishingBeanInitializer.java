package io.zhijun.spring.context.event;

import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.zhijun.spring.constants.PropertyConstants.EVENT_PUBLISHING_BEAN_PROPERTY_KEY_PREFIX;

/**
 * 注册 {@link EventPublishingBeanBeforeProcessor}，启用 bean 生命周期事件发布。
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

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        context.addBeanFactoryPostProcessor(new EventPublishingBeanBeforeProcessor());
    }

    @Override
    protected boolean isEnabled(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        return environment.getProperty(EVENT_PUBLISHING_BEAN_PROPERTY_KEY_PREFIX, boolean.class, true);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
