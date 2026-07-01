package io.zhijun.spring.web.metadata;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * 智能 {@link WebEndpointMappingFactory}，自动发现所有 {@link WebEndpointMappingFactory} SPI 和 Bean 并委托给它们。
 */
public class SmartWebEndpointMappingFactory extends AbstractWebEndpointMappingFactory<Object> {

    private final Map<Class<?>, List<WebEndpointMappingFactory<?>>> delegates = new LinkedHashMap<>();

    public SmartWebEndpointMappingFactory(ConfigurableListableBeanFactory beanFactory) {
        initDelegates(beanFactory);
    }

    @SuppressWarnings("unchecked")
    private void initDelegates(ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory != null) {
            Map<String, WebEndpointMappingFactory> beans = beanFactory.getBeansOfType(WebEndpointMappingFactory.class);
            for (WebEndpointMappingFactory factory : beans.values()) {
                if (factory != this) {
                    addDelegate(factory);
                }
            }
        }
    }

    void addDelegate(WebEndpointMappingFactory<?> factory) {
        Class<?> sourceType = factory.getSourceType();
        delegates.computeIfAbsent(sourceType, k -> new ArrayList<>()).add(factory);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected WebEndpointMapping doCreate(Object endpoint) throws Throwable {
        List<WebEndpointMappingFactory<?>> factories = delegates.get(endpoint.getClass());
        if (factories == null || factories.isEmpty()) {
            return null;
        }
        WebEndpointMapping result = null;
        for (WebEndpointMappingFactory<?> factory : factories) {
            WebEndpointMapping mapping = createFromFactory(factory, endpoint);
            if (mapping != null) {
                result = mapping;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> WebEndpointMapping createFromFactory(WebEndpointMappingFactory<T> factory, Object endpoint) throws Throwable {
        T typedEndpoint = (T) endpoint;
        if (factory.supports(typedEndpoint)) {
            Optional<WebEndpointMapping> optional = factory.create(typedEndpoint);
            return optional.orElse(null);
        }
        return null;
    }
}
