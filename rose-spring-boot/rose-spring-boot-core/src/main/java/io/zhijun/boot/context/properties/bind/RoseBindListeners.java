package io.zhijun.boot.context.properties.bind;

import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

final class RoseBindListeners implements RoseBindListener {

    private final Iterable<RoseBindListener> listeners;

    RoseBindListeners(Iterable<RoseBindListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onStart(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
        for (RoseBindListener listener : listeners) {
            listener.onStart(name, target, context);
        }
    }

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        for (RoseBindListener listener : listeners) {
            listener.onSuccess(name, target, context, result);
        }
    }

    @Override
    public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        for (RoseBindListener listener : listeners) {
            listener.onCreate(name, target, context, result);
        }
    }

    @Override
    public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
        for (RoseBindListener listener : listeners) {
            listener.onFailure(name, target, context, error);
        }
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        for (RoseBindListener listener : listeners) {
            listener.onFinish(name, target, context, result);
        }
    }
}
