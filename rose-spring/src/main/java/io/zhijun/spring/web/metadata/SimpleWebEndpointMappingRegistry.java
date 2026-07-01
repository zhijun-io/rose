package io.zhijun.spring.web.metadata;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 基于 {@link Set} 的简单 {@link WebEndpointMappingRegistry} 实现。
 */
public class SimpleWebEndpointMappingRegistry extends FilteringWebEndpointMappingRegistry {

    private final Set<WebEndpointMapping> repository = new LinkedHashSet<>();

    @Override
    protected boolean doRegister(WebEndpointMapping mapping) {
        return repository.add(mapping);
    }

    @Override
    public Collection<WebEndpointMapping> getAll() {
        return repository;
    }
}
