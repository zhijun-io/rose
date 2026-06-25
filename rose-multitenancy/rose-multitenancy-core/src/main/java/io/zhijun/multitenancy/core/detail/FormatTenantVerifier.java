package io.zhijun.multitenancy.core.detail;

import io.zhijun.core.annotation.Incubating;
import io.zhijun.multitenancy.core.exception.TenantVerificationException;

/**
 * Verifies tenant identifier format only (no tenant registry lookup).
 * <p>
 * Registered by default when no {@link TenantDetailsService} is configured, so HTTP
 * resolution cannot accept arbitrary header values in production.
 */
@Incubating
public final class FormatTenantVerifier implements TenantVerifier {

    private static final String TENANT_ID_PATTERN = "[a-zA-Z0-9_-]+";

    @Override
    public void verify(String tenantIdentifier) {
        if (tenantIdentifier == null || !tenantIdentifier.matches(TENANT_ID_PATTERN)) {
            throw new TenantVerificationException(
                    "The tenant identifier must contain only alphanumeric characters, dashes (-), and underscores (_)");
        }
    }

}
