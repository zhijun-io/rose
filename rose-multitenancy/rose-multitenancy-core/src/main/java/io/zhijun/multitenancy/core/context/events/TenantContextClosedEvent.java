package io.zhijun.multitenancy.core.context.events;

import io.zhijun.core.support.Incubating;

/**
 * A {@link TenantEvent} which indicates the context for the current tenant has been
 * closed.
 */
@Incubating
public final class TenantContextClosedEvent extends TenantEvent {

    public TenantContextClosedEvent(String tenantIdentifier, Object object) {
        super(tenantIdentifier, object);
    }

}
