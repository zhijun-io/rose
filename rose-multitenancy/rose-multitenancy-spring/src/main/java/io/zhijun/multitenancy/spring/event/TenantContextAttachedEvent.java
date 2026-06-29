package io.zhijun.multitenancy.spring.event;

import org.apiguardian.api.API;

/**
 * Indicates a multitenancy has been attached to the current context.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class TenantContextAttachedEvent extends TenantEvent {

    public TenantContextAttachedEvent(String tenantIdentifier, Object source) {
        super(tenantIdentifier, source);
    }
}
