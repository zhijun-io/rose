package io.zhijun.multitenancy.spring.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;

import io.zhijun.annotation.Incubating;
import io.zhijun.multitenancy.core.MultitenancyDefaults;
import io.zhijun.multitenancy.spring.event.TenantContextAttachedEvent;
import io.zhijun.multitenancy.spring.event.TenantContextClosedEvent;

/**
 * Manages the SLF4J {@link MDC} multitenancy identifier in response to multitenancy context events.
 */
@Incubating
public final class MdcTenantEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MdcTenantEventListener.class);

    private static final String DEFAULT_TENANT_IDENTIFIER_KEY = MultitenancyDefaults.DEFAULT_MDC_TENANT_KEY;

    private final String tenantIdentifierKey;

    public MdcTenantEventListener() {
        this(DEFAULT_TENANT_IDENTIFIER_KEY);
    }

    public MdcTenantEventListener(String tenantIdentifierKey) {
        Assert.hasText(tenantIdentifierKey, "tenantIdentifierKey cannot be null or empty");
        this.tenantIdentifierKey = tenantIdentifierKey;
    }

    @EventListener
    void onAttached(TenantContextAttachedEvent event) {
        logger.trace("Setting current multitenancy in MDC to: {}", event.getTenantIdentifier());
        MDC.put(tenantIdentifierKey, event.getTenantIdentifier());
    }

    @EventListener
    void onClosed(TenantContextClosedEvent event) {
        logger.trace("Removing current multitenancy from MDC");
        MDC.remove(tenantIdentifierKey);
    }
}
