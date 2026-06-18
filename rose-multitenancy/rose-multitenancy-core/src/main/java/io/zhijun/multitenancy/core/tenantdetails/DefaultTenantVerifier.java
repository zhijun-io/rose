package io.zhijun.multitenancy.core.tenantdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.zhijun.core.annotation.Incubating;
import io.zhijun.multitenancy.core.exceptions.TenantVerificationException;

/**
 * Default {@link TenantVerifier} implementation that checks the tenant exists and is
 * enabled using a {@link TenantDetailsService}.
 */
@Incubating
public final class DefaultTenantVerifier implements TenantVerifier {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTenantVerifier.class);

    private final TenantDetailsService tenantDetailsService;

    public DefaultTenantVerifier(TenantDetailsService tenantDetailsService) {
        Assert.notNull(tenantDetailsService, "tenantDetailsService cannot be null");
        this.tenantDetailsService = tenantDetailsService;
    }

    @Override
    public void verify(String tenantIdentifier) {
        if (tenantIdentifier == null || !tenantIdentifier.matches("[a-zA-Z0-9_-]+")) {
            throw new TenantVerificationException("The tenant identifier must contain only alphanumeric characters, dashes (-), and underscores (_)");
        }
        logger.trace("Verifying tenant: {}", tenantIdentifier);
        TenantDetails tenant = tenantDetailsService.loadTenantByIdentifier(tenantIdentifier);
        if (tenant == null || !tenant.isEnabled()) {
            throw new TenantVerificationException("The resolved tenant is invalid or disabled");
        }
    }

}
