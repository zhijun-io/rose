package io.zhijun.multitenancy.spring.event;

import io.zhijun.core.annotation.Incubating;

/**
 * Indicates a multitenancy has been attached to the current context.
 */
@Incubating
public final class TenantContextAttachedEvent extends TenantEvent {

    public TenantContextAttachedEvent(String tenantIdentifier, Object source) {
        super(tenantIdentifier, source);
    }

}
