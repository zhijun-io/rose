package io.zhijun.spring.core.propertysource.watch;

import org.springframework.core.io.Resource;

/**
 * Callback when a file-backed property source location changes.
 */
public interface PropertySourceReloadCallback {

    void onReload(String resourceValue, Resource resource) throws Throwable;
}
