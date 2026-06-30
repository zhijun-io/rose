package io.zhijun.spring.web.event;

import io.zhijun.spring.web.metadata.WebEndpointMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

import java.util.Collection;
import java.util.Collections;

/**
 * WebEndpointMapping 就绪事件，在所有端点映射注册完成后发布。
 */
public class WebEndpointMappingsReadyEvent extends ApplicationContextEvent {

    private final Collection<WebEndpointMapping> mappings;

    public WebEndpointMappingsReadyEvent(ApplicationContext source, Collection<WebEndpointMapping> mappings) {
        super(source);
        this.mappings = Collections.unmodifiableCollection(mappings);
    }

    public Collection<WebEndpointMapping> getMappings() {
        return mappings;
    }

    @Override
    public String toString() {
        return "WebEndpointMappingsReadyEvent{" +
                "source=" + source +
                ", mappings=" + mappings +
                '}';
    }
}
