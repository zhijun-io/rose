package io.zhijun.multitenancy.core.detail;

import org.apiguardian.api.API;

/**
 * Constraints applied to tenant identifiers.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class TenantIdentifierConstraints {

    public static final String PATTERN = "[a-zA-Z0-9_-]+";

    private TenantIdentifierConstraints() {}
}
