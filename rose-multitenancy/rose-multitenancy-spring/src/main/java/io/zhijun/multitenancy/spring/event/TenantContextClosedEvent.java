package io.zhijun.multitenancy.spring.event;

import io.zhijun.annotation.Incubating;

/**
 * Indicates the context for the current multitenancy has been closed.
 */
@Incubating
public final class TenantContextClosedEvent extends TenantEvent {

    public TenantContextClosedEvent(String tenantIdentifier, Object source) {
        super(tenantIdentifier, source);
    }
}
