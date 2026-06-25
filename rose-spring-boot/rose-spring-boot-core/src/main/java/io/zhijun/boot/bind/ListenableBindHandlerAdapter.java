package io.zhijun.boot.bind;

import io.zhijun.annotation.Incubating;
import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * {@link BindHandler} that notifies {@link RoseBindListener} instances during binding.
 */
@Incubating
public final class ListenableBindHandlerAdapter extends AbstractBindHandler {

    private final RoseBindListeners bindListeners;

    public ListenableBindHandlerAdapter(Iterable<RoseBindListener> bindListeners) {
        this(DEFAULT, bindListeners);
    }

    public ListenableBindHandlerAdapter(BindHandler parent, Iterable<RoseBindListener> bindListeners) {
        super(parent);
        this.bindListeners = new RoseBindListeners(bindListeners);
    }

    @Override
    public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        Bindable<T> result = super.onStart(name, target, context);
        bindListeners.onStart(name, target, context);
        return result;
    }

    @Override
    public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        Object returnValue = super.onSuccess(name, target, context, result);
        bindListeners.onSuccess(name, target, context, result);
        return returnValue;
    }

    @Override
    public Object onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        Object returnValue = super.onCreate(name, target, context, result);
        bindListeners.onCreate(name, target, context, result);
        return returnValue;
    }

    @Override
    public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)
            throws Exception {
        try {
            return super.onFailure(name, target, context, error);
        }
        catch (Exception ex) {
            bindListeners.onFailure(name, target, context, error);
            throw ex;
        }
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)
            throws Exception {
        super.onFinish(name, target, context, result);
        bindListeners.onFinish(name, target, context, result);
    }
}
