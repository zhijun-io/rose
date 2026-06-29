package io.zhijun.multitenancy.spring.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;



/**
 * Abstract superclass for all multitenancy-related Spring application events.
 */

public abstract class TenantEvent extends ApplicationEvent {

    private final String tenantIdentifier;

    public TenantEvent(String tenantIdentifier, Object source) {
        super(source);
        Assert.hasText(tenantIdentifier, "tenantIdentifier cannot be null or empty");
        this.tenantIdentifier = tenantIdentifier;
    }

    public String getTenantIdentifier() {
        return tenantIdentifier;
    }
}
