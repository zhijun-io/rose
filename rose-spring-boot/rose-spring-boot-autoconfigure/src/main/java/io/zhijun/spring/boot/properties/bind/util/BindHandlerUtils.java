package io.zhijun.spring.boot.properties.bind.util;

import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;

import static org.springframework.boot.context.properties.bind.BindHandler.DEFAULT;

/**
 * {@link BindHandler} 工具类
 */
public abstract class BindHandlerUtils {

    public static BindHandler createBindHandler(boolean ignoreUnknownFields, boolean ignoreInvalidFields) {
        BindHandler handler = DEFAULT;
        if (ignoreInvalidFields) {
            handler = new IgnoreErrorsBindHandler(handler);
        }
        if (!ignoreUnknownFields) {
            UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
            handler = new NoUnboundElementsBindHandler(handler, filter);
        }
        return handler;
    }

    private BindHandlerUtils() {
    }
}
