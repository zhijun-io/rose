package io.zhijun.local.core.registration;

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
public final class LocalServiceDynamicPropertySource extends MapPropertySource {

    public static final String PROPERTY_SOURCE_NAME = "Rose Local Dynamic Properties";

    private final Map<String, Supplier<Object>> valueSuppliers;

    LocalServiceDynamicPropertySource(Map<String, Supplier<Object>> valueSuppliers) {
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

    public static LocalServiceDynamicPropertySource getOrCreate(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource<?> existingPropertySource = propertySources.get(PROPERTY_SOURCE_NAME);
        if (existingPropertySource instanceof LocalServiceDynamicPropertySource) {
            return (LocalServiceDynamicPropertySource) existingPropertySource;
        }
        if (existingPropertySource == null) {
            LocalServiceDynamicPropertySource propertySource = new LocalServiceDynamicPropertySource(
                    Collections.synchronizedMap(new LinkedHashMap<String, Supplier<Object>>()));
            propertySources.addFirst(propertySource);
            return propertySource;
        }
        throw new IllegalStateException(
                "PropertySource with name '" + PROPERTY_SOURCE_NAME + "' must be a DevServiceDynamicPropertySource");
    }
}
