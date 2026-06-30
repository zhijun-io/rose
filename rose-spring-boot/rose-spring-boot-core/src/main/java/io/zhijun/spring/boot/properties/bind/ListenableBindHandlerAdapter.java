 package io.zhijun.spring.boot.properties.bind;

 import org.springframework.boot.context.properties.bind.AbstractBindHandler;
 import org.springframework.boot.context.properties.bind.BindContext;
 import org.springframework.boot.context.properties.bind.BindHandler;
 import org.springframework.boot.context.properties.bind.Bindable;
 import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

 import java.util.List;

 /**
 * Adapts a chain of {@link BindListener}s into a single {@link BindHandler}.
 * <p>
 * Delegates each lifecycle event to all registered listeners in order.
 * <p>
 * Inspired by {@code io.microsphere.spring.boot.context.properties.bind.ListenableBindHandlerAdapter}.
 */
public class ListenableBindHandlerAdapter extends AbstractBindHandler {
    private final List<BindListener> listeners;

    public ListenableBindHandlerAdapter(BindHandler parent, List<BindListener> listeners) {
         super(parent);
         this.listeners = listeners;
     }

    @Override
    public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        for (BindListener listener : listeners) {
            listener.onStart(name, target, context);
        }
        return super.onStart(name, target, context);
    }

    @Override
    public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        for (BindListener listener : listeners) {
            result = listener.onSuccess(name, target, context, result);
        }
        return super.onSuccess(name, target, context, result);
    }

    @Override
    public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)
            throws Exception {
        for (BindListener listener : listeners) {
            listener.onFailure(name, target, context, error);
        }
        return super.onFailure(name, target, context, error);
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) throws Exception {
        super.onFinish(name, target, context, result);
        for (BindListener listener : listeners) {
            listener.onFinish(name, target, context, result);
        }
    }
}
