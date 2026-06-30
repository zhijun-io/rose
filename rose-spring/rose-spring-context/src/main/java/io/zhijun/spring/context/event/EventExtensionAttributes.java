package io.zhijun.spring.context.event;

import io.zhijun.spring.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.zhijun.spring.beans.BeanSource;

/**
 * {@link EnableEventExtension} 注解属性。
 */
class EventExtensionAttributes extends ResolvablePlaceholderAnnotationAttributes<EnableEventExtension> {

    static final String INTERCEPTED_ATTRIBUTE_NAME = "intercepted";

    static final String EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME = "executorForListener";

    EventExtensionAttributes(ResolvablePlaceholderAnnotationAttributes<EnableEventExtension> annotationAttributes) {
        super(annotationAttributes, null);
    }

    public boolean isIntercepted() {
        return getBoolean(INTERCEPTED_ATTRIBUTE_NAME);
    }

    public String getExecutorForListener() {
        return getString(EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME);
    }

    public BeanSource[] getSources() {
        return (BeanSource[]) get("sources");
    }
}
