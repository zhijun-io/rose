package io.zhijun.devservice.boot.autoconfigure;

import io.zhijun.devservice.core.api.provider.DevServiceCategory;

import java.util.List;

/**
 * Thrown when multiple dev services in the same category are active.
 */
public class MultipleDevServiceException extends RuntimeException {

    private final DevServiceCategory category;
    private final List<String> serviceNames;

    public MultipleDevServiceException(DevServiceCategory category, List<String> serviceNames) {
        super("Multiple " + category.id() + " dev services detected: " + serviceNames);
        this.category = category;
        this.serviceNames = serviceNames;
    }

    public DevServiceCategory getCategory() {
        return category;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }
}
