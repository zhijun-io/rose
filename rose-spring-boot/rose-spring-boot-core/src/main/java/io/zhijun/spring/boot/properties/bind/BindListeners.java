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
    public void onStart(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
        iterate(listener -> listener.onStart(name, target, context));
    }

    @Override
    public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        for (BindListener listener : listeners) {
            result = listener.onSuccess(name, target, context, result);
        }
        return result;
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
