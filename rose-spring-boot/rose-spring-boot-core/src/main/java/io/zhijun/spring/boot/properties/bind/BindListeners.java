package io.zhijun.spring.boot.properties.bind;

import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

import java.util.function.Consumer;

/**
 * 复合 {@link BindListener}
 */
class BindListeners implements BindListener {

    private final Iterable<BindListener> listeners;

    BindListeners(Iterable<BindListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        iterate(listener -> listener.onStart(name, target, context));
    }

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onSuccess(name, target, context, result));
    }

    @Override
    public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onCreate(name, target, context, result));
    }

    @Override
    public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
        iterate(listener -> listener.onFailure(name, target, context, error));
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onFinish(name, target, context, result));
    }

    private void iterate(Consumer<BindListener> listenerConsumer) {
        listeners.forEach(listenerConsumer);
    }
}
