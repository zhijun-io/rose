package io.zhijun.multitenancy.core.detail;



/**
 * Constraints applied to tenant identifiers.
 */

public final class TenantIdentifierConstraints {

    public static final String PATTERN = "[a-zA-Z0-9_-]+";

    private TenantIdentifierConstraints() {}
}
