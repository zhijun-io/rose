package io.zhijun.spring.web.metadata;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.util.List;
import java.util.function.Predicate;

/**
 * 支持过滤的抽象 {@link WebEndpointMappingRegistry}。
 */
public abstract class FilteringWebEndpointMappingRegistry implements WebEndpointMappingRegistry, BeanFactoryAware {

    protected static final Predicate<WebEndpointMapping> DEFAULT_FILTER = e -> true;

    protected Predicate<WebEndpointMapping> filter = DEFAULT_FILTER;

    protected BeanFactory beanFactory;

    @Override
    public final boolean register(WebEndpointMapping mapping) {
        if (filter.test(mapping)) {
            return doRegister(mapping);
        }
        return false;
    }

    public void setFilter(Predicate<WebEndpointMapping> filter) {
        this.filter = filter != null ? filter : DEFAULT_FILTER;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    protected abstract boolean doRegister(WebEndpointMapping mapping);
}
