package io.zhijun.multitenancy.core.detail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.zhijun.core.annotation.Incubating;

/**
 * Default implementation of {@link TenantDetails}.
 */
@Incubating
public final class Tenant implements TenantDetails {

    private final String identifier;

    private final boolean enabled;

    private final Map<String, Object> attributes;

    public Tenant(String identifier, boolean enabled, Map<String, Object> attributes) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier cannot be null or empty");
        }
        Objects.requireNonNull(attributes, "attributes cannot be null");
        for (String key : attributes.keySet()) {
            if (key == null) {
                throw new IllegalArgumentException("attributes keys cannot contain null values");
            }
        }
        this.identifier = identifier;
        this.enabled = enabled;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String identifier;

        private boolean enabled = true;

        private Map<String, Object> attributes = new HashMap<String, Object>();

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder addAttribute(String key, Object value) {
            attributes.put(key, value);
            return this;
        }

        public Tenant build() {
            return new Tenant(identifier, enabled, attributes);
        }
    }

}
