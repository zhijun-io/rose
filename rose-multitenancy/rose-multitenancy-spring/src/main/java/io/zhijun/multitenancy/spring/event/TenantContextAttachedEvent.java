package io.zhijun.multitenancy.spring.event;



/**
 * Indicates a multitenancy has been attached to the current context.
 */

public final class TenantContextAttachedEvent extends TenantEvent {

    public TenantContextAttachedEvent(String tenantIdentifier, Object source) {
        super(tenantIdentifier, source);
    }
}
