package io.zhijun.multitenancy.core.detail;

import io.zhijun.multitenancy.core.exception.TenantVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Default {@link TenantVerifier} implementation that checks the multitenancy exists and is
 * enabled using a {@link TenantDetailsService}.
 */

public final class DefaultTenantVerifier implements TenantVerifier {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTenantVerifier.class);

    private final TenantDetailsService tenantDetailsService;

    public DefaultTenantVerifier(TenantDetailsService tenantDetailsService) {
        this.tenantDetailsService = Objects.requireNonNull(tenantDetailsService, "tenantDetailsService cannot be null");
    }

    @Override
    public void verify(String tenantIdentifier) {
        if (tenantIdentifier == null || !tenantIdentifier.matches(TenantIdentifierConstraints.PATTERN)) {
            throw new TenantVerificationException(
                    "The tenant identifier must contain only alphanumeric characters, dashes (-), and underscores (_)");
        }
        logger.trace("Verifying multitenancy: {}", tenantIdentifier);
        TenantDetails tenant = tenantDetailsService.loadTenantByIdentifier(tenantIdentifier);
        if (tenant == null || !tenant.isEnabled()) {
            throw new TenantVerificationException("The resolved multitenancy is invalid or disabled");
        }
    }
}
