package io.zhijun.spring.core.env.refresh;

import org.springframework.core.io.Resource;

/**
 * Refreshes property sources backed by a resource.
 */
public interface ResourcePropertySourcesRefresher {

    void refresh(String resourceValue, Resource resource) throws Throwable;
}
