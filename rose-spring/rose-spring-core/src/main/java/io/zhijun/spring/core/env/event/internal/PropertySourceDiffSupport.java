package io.zhijun.spring.core.env.event.internal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * Utilities for diffing {@link PropertySource} keys.
 */
public final class PropertySourceDiffSupport {

    private PropertySourceDiffSupport() {}

    public static Set<String> getPropertyNames(PropertySource<?> source) {
        if (!(source instanceof EnumerablePropertySource)) {
            return Collections.emptySet();
        }
        EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
        LinkedHashSet<String> names = new LinkedHashSet<String>();
        for (String name : enumerable.getPropertyNames()) {
            names.add(name);
        }
        return names;
    }

    public static Set<String> diffReplaced(PropertySource<?> oldSource, PropertySource<?> newSource) {
        Set<String> oldNames = getPropertyNames(oldSource);
        Set<String> newNames = getPropertyNames(newSource);
        if (oldNames.isEmpty() && newNames.isEmpty()) {
            return Collections.emptySet();
        }
        LinkedHashSet<String> changed = new LinkedHashSet<String>();
        if (!oldNames.isEmpty() && !newNames.isEmpty()) {
            for (String key : oldNames) {
                if (!newNames.contains(key)) {
                    changed.add(key);
                }
            }
            for (String key : newNames) {
                if (!oldNames.contains(key)) {
                    changed.add(key);
                } else if (!Objects.equals(oldSource.getProperty(key), newSource.getProperty(key))) {
                    changed.add(key);
                }
            }
            return changed;
        }
        Set<String> keys = !oldNames.isEmpty() ? oldNames : newNames;
        for (String key : keys) {
            if (!Objects.equals(oldSource.getProperty(key), newSource.getProperty(key))) {
                changed.add(key);
            }
        }
        return changed;
    }

    public static Set<String> keysAdded(PropertySource<?> newSource) {
        return getPropertyNames(newSource);
    }

    public static Set<String> keysRemoved(PropertySource<?> oldSource) {
        return getPropertyNames(oldSource);
    }
}
