package io.zhijun.multitenancy.core.context.event;

import io.zhijun.core.annotation.Incubating;

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
