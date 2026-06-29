package io.zhijun.multitenancy.spring.event;

import org.apiguardian.api.API;

/**
 * Indicates the context for the current multitenancy has been closed.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class TenantContextClosedEvent extends TenantEvent {

    public TenantContextClosedEvent(String tenantIdentifier, Object source) {
        super(tenantIdentifier, source);
    }
}
