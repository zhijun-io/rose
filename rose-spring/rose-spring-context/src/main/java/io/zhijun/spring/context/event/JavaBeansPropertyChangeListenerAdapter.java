package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEventPublisher;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * 将 JavaBeans {@link PropertyChangeListener} 桥接到 Spring 事件发布机制的适配器。
 * <p>
 * 将 JavaBeans {@link PropertyChangeEvent} 转为 Spring {@link BeanPropertyChangedEvent} 并发布。
 * <p>
 * （移植自 microsphere-spring {@code JavaBeansPropertyChangeListenerAdapter}）
 *
 * @see BeanPropertyChangedEvent
 * @see PropertyChangeListener
 */
public class JavaBeansPropertyChangeListenerAdapter implements PropertyChangeListener {

    private final ApplicationEventPublisher applicationEventPublisher;

    public JavaBeansPropertyChangeListenerAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        BeanPropertyChangedEvent adaptedEvent = adapt(event);
        applicationEventPublisher.publishEvent(adaptedEvent);
    }

    private BeanPropertyChangedEvent adapt(PropertyChangeEvent event) {
        return new BeanPropertyChangedEvent(event.getSource(), event.getPropertyName(),
                event.getOldValue(), event.getNewValue());
    }
}
