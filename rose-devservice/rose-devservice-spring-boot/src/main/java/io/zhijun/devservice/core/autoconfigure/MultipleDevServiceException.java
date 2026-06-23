package io.zhijun.devservice.core.autoconfigure;

import java.util.List;

/**
 * Thrown when multiple dev services in the same category are active.
 */
public class MultipleDevServiceException extends RuntimeException {

    private final String category;
    private final List<String> serviceNames;

    public MultipleDevServiceException(String category, List<String> serviceNames) {
        super("Multiple " + category + " dev services detected: " + serviceNames);
        this.category = category;
        this.serviceNames = serviceNames;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }
}
