package io.zhijun.multitenancy.core.tenantdetails;

import java.util.Collections;
import java.util.Map;

/**
 * Provides core tenant information.
 */
public interface TenantDetails {

    String getIdentifier();

    boolean isEnabled();

    default Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

}
