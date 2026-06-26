package io.zhijun.multitenancy.core.detail;

import io.zhijun.annotation.Incubating;

/**
 * Constraints applied to tenant identifiers.
 */
@Incubating
public final class TenantIdentifierConstraints {

    public static final String PATTERN = "[a-zA-Z0-9_-]+";

    private TenantIdentifierConstraints() {
    }

}
