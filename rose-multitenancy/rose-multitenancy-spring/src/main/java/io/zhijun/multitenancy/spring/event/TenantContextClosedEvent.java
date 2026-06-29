package io.zhijun.multitenancy.spring.event;



/**
 * Indicates the context for the current multitenancy has been closed.
 */

public final class TenantContextClosedEvent extends TenantEvent {

    public TenantContextClosedEvent(String tenantIdentifier, Object source) {
        super(tenantIdentifier, source);
    }
}
