package io.zhijun.spring.core.env.support;

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

    private PropertySourceDiffSupport() {
    }

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
        if (!(oldSource instanceof EnumerablePropertySource)
                && !(newSource instanceof EnumerablePropertySource)) {
            return Collections.emptySet();
        }
        LinkedHashSet<String> changed = new LinkedHashSet<String>();
        if (oldSource instanceof EnumerablePropertySource) {
            EnumerablePropertySource<?> oldEnumerable = (EnumerablePropertySource<?>) oldSource;
            for (String key : oldEnumerable.getPropertyNames()) {
                if (newSource.getProperty(key) == null) {
                    changed.add(key);
                }
            }
        }
        if (newSource instanceof EnumerablePropertySource) {
            EnumerablePropertySource<?> newEnumerable = (EnumerablePropertySource<?>) newSource;
            for (String key : newEnumerable.getPropertyNames()) {
                Object oldValue = oldSource.getProperty(key);
                Object newValue = newEnumerable.getProperty(key);
                if (oldValue == null || !Objects.equals(oldValue, newValue)) {
                    changed.add(key);
                }
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
