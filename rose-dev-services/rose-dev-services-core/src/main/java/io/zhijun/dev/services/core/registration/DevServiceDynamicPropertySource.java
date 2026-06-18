package io.zhijun.dev.services.core.registration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import io.zhijun.core.annotation.Incubating;

/**
 * Lazy dynamic property source for dev services.
 * <p>
 * Dev service connection properties take precedence over manually configured values during
 * development and testing, matching Arconia semantics.
 */
@Incubating
public final class DevServiceDynamicPropertySource extends MapPropertySource {

    public static final String PROPERTY_SOURCE_NAME = "Rose Dev Services Dynamic Properties";

    private final Map<String, Supplier<Object>> valueSuppliers;

    DevServiceDynamicPropertySource(Map<String, Supplier<Object>> valueSuppliers) {
        super(PROPERTY_SOURCE_NAME, Collections.unmodifiableMap(valueSuppliers));
        this.valueSuppliers = valueSuppliers;
    }

    public void add(String name, Supplier<Object> valueSupplier) {
        Assert.hasText(name, "name cannot be null or empty");
        Assert.notNull(valueSupplier, "valueSupplier cannot be null");
        this.valueSuppliers.put(name, valueSupplier);
    }

    @Override
    public Object getProperty(String name) {
        Supplier<Object> supplier = valueSuppliers.get(name);
        return supplier != null ? supplier.get() : null;
    }

    public static DevServiceDynamicPropertySource getOrCreate(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource<?> existingPropertySource = propertySources.get(PROPERTY_SOURCE_NAME);
        if (existingPropertySource instanceof DevServiceDynamicPropertySource) {
            return (DevServiceDynamicPropertySource) existingPropertySource;
        }
        if (existingPropertySource == null) {
            DevServiceDynamicPropertySource propertySource = new DevServiceDynamicPropertySource(
                    Collections.synchronizedMap(new LinkedHashMap<String, Supplier<Object>>()));
            propertySources.addFirst(propertySource);
            return propertySource;
        }
        throw new IllegalStateException(
                "PropertySource with name '" + PROPERTY_SOURCE_NAME + "' must be a DevServiceDynamicPropertySource");
    }
}
