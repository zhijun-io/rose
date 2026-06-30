package io.zhijun.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.context.event.SmartApplicationListener;

/**
 * 组合 {@link GenericApplicationListener} + {@link SmartApplicationListener} 的适配接口。
 * <p>
 * （借鉴 microsphere-spring {@code GenericApplicationListenerAdapter}）
 *
 * @see GenericApplicationListener
 * @see SmartApplicationListener
 */
public interface GenericApplicationListenerAdapter extends GenericApplicationListener, SmartApplicationListener {

    @Override
    default boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    default boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return true;
    }

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    default String getListenerId() {
        return "";
    }
}
