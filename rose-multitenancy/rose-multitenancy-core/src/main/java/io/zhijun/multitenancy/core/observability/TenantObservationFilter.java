package io.zhijun.multitenancy.core.observability;

import java.util.Objects;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;

import io.zhijun.core.annotation.Incubating;
import io.zhijun.multitenancy.core.context.TenantContext;

/**
 * An {@link ObservationFilter} that enriches all observations with the current tenant
 * identifier from the {@link TenantContext}.
 */
@Incubating
public final class TenantObservationFilter implements ObservationFilter {

    static final String DEFAULT_TENANT_IDENTIFIER_KEY = "tenant.id";

    private final String tenantIdentifierKey;

    private final Cardinality cardinality;

    public TenantObservationFilter() {
        this(DEFAULT_TENANT_IDENTIFIER_KEY, Cardinality.HIGH);
    }

    public TenantObservationFilter(String tenantIdentifierKey, Cardinality cardinality) {
        if (tenantIdentifierKey == null || tenantIdentifierKey.trim().isEmpty()) {
            throw new IllegalArgumentException("tenantIdentifierKey cannot be null or empty");
        }
        this.tenantIdentifierKey = tenantIdentifierKey;
        this.cardinality = Objects.requireNonNull(cardinality, "cardinality cannot be null");
    }

    public String getTenantIdentifierKey() {
        return tenantIdentifierKey;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    @Override
    public Observation.Context map(Observation.Context context) {
        String tenantIdentifier = TenantContext.getTenantId();
        if (tenantIdentifier == null) {
            return context;
        }

        KeyValue keyValue = KeyValue.of(tenantIdentifierKey, tenantIdentifier);
        if (cardinality == Cardinality.LOW) {
            context.addLowCardinalityKeyValue(keyValue);
        } else {
            context.addHighCardinalityKeyValue(keyValue);
        }

        return context;
    }

}
