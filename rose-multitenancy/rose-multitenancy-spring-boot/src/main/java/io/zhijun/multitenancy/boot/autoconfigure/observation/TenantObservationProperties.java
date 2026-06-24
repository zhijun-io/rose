package io.zhijun.multitenancy.boot.autoconfigure.observation;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.multitenancy.core.observation.Cardinality;

/**
 * Configuration properties for multitenancy observation enrichment.
 */
@ConfigurationProperties(prefix = TenantObservationProperties.CONFIG_PREFIX)
public class TenantObservationProperties {

    public static final String CONFIG_PREFIX = "rose.multitenancy.observations";

    /**
     * Whether observations are enhanced with multitenancy information.
     */
    private boolean enabled = true;

    /**
     * Name of the key to use for the multitenancy identifier in observations.
     */
    private String keyName = "multitenancy.id";

    /**
     * The cardinality of the multitenancy identifier key value. {@code HIGH} (default) adds it
     * as a high-cardinality key value, appearing only in traces. {@code LOW} adds it as a
     * low-cardinality key value, appearing in both metrics and traces.
     */
    private Cardinality cardinality = Cardinality.HIGH;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

}
