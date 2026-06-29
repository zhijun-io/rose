package io.zhijun.multitenancy.core.detail;

import java.util.Collections;
import java.util.Map;

/**
 * Provides core multitenancy information.
 */
public interface TenantDetails {

    String getIdentifier();

    boolean isEnabled();

    default Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }
}
