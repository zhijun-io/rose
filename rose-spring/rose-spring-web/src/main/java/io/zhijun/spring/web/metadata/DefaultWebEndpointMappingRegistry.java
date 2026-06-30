package io.zhijun.spring.web.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 基于 {@link Set} 的默认 {@link WebEndpointMappingRegistry} 实现。
 *
 * @since 1.0.0
 */
public class DefaultWebEndpointMappingRegistry implements WebEndpointMappingRegistry {

    private final Set<WebEndpointMapping> mappings = new LinkedHashSet<>();

    @Override
    public boolean register(WebEndpointMapping mapping) {
        return mapping != null && this.mappings.add(mapping);
    }

    @Override
    public Collection<WebEndpointMapping> getAll() {
        return Collections.unmodifiableSet(mappings);
    }
}
