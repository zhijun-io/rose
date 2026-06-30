package io.zhijun.spring.web.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * HandlerMethod 参数解析完成事件。
 */
public class HandlerMethodArgumentsResolvedEvent extends ApplicationEvent {

    private final transient HandlerMethod handlerMethod;

    private final transient Object[] arguments;

    public HandlerMethodArgumentsResolvedEvent(WebRequest webRequest, HandlerMethod handlerMethod, Object... arguments) {
        super(webRequest);
        Objects.requireNonNull(handlerMethod, "'handlerMethod' must not be null");
        Objects.requireNonNull(arguments, "'arguments' must not be null");
        this.handlerMethod = handlerMethod;
        this.arguments = arguments;
    }

    public HandlerMethod getHandlerMethod() {
        return handlerMethod;
    }

    public Method getMethod() {
        return handlerMethod.getMethod();
    }

    public Object[] getArguments() {
        return arguments;
    }

    public WebRequest getWebRequest() {
        return (WebRequest) getSource();
    }

    @Override
    public String toString() {
        return "HandlerMethodArgumentsResolvedEvent{" +
                "source=" + getSource() +
                ", handlerMethod=" + handlerMethod +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}
