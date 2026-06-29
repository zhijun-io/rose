package io.zhijun.spring.core.propertysource;

import org.springframework.core.io.Resource;

/**
 * Callback triggered when a watched property-source resource changes.
 */
public interface PropertySourceReloadCallback {

    void onReload(String resourceValue, Resource resource) throws Throwable;
}
