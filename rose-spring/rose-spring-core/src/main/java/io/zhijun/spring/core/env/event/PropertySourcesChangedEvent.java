package io.zhijun.spring.core.env.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.PropertySource;

import io.zhijun.spring.core.env.event.internal.PropertySourceDiffSupport;

/**
 * Bulk property source change event.
 */
public class PropertySourcesChangedEvent extends ApplicationContextEvent {

    private final List<PropertySourceChangedEvent> subEvents;

    private transient Set<String> changedKeys;

    public PropertySourcesChangedEvent(ApplicationContext source, List<PropertySourceChangedEvent> subEvents) {
        super(source);
        this.subEvents = subEvents;
    }

    public List<PropertySourceChangedEvent> getSubEvents() {
        return Collections.unmodifiableList(subEvents);
    }

    public boolean contains(PropertySourceChangedEvent.Kind kind) {
        for (PropertySourceChangedEvent event : subEvents) {
            if (event.getKind() == kind) {
                return true;
            }
        }
        return false;
    }

    public List<PropertySource<?>> getNewPropertySources() {
        List<PropertySource<?>> propertySources = new ArrayList<PropertySource<?>>();
        for (PropertySourceChangedEvent event : subEvents) {
            PropertySource<?> propertySource = event.getNewPropertySource();
            if (propertySource != null) {
                propertySources.add(propertySource);
            }
        }
        return Collections.unmodifiableList(propertySources);
    }

    /**
     * Key-level changes. For selective refresh prefer this over {@link #getChangedProperties()}.
     * <ul>
     * <li>ADDED: all keys in the new source</li>
     * <li>REMOVED: all keys in the old source</li>
     * <li>REPLACED: {@link PropertySourceDiffSupport#diffReplaced}</li>
     * </ul>
     */
    public Set<String> getChangedKeys() {
        if (changedKeys == null) {
            changedKeys = computeChangedKeys();
        }
        return changedKeys;
    }

    /**
     * Returns properties from ADDED/REPLACED sources (full map). For key-level diff use
     * {@link #getChangedKeys()}.
     */
    public Map<String, Object> getChangedProperties() {
        return getProperties(PropertySourceChangedEvent.Kind.ADDED, PropertySourceChangedEvent.Kind.REPLACED);
    }

    public Map<String, Object> getAddedProperties() {
        return getProperties(PropertySourceChangedEvent.Kind.ADDED);
    }

    public Map<String, Object> getRemovedProperties() {
        return getProperties(PropertySourceChangedEvent.Kind.REMOVED);
    }

    private Map<String, Object> getProperties(PropertySourceChangedEvent.Kind... kinds) {
        java.util.LinkedHashMap<String, Object> properties = new java.util.LinkedHashMap<String, Object>();
        for (PropertySourceChangedEvent event : subEvents) {
            if (!contains(kinds, event.getKind())) {
                continue;
            }
            PropertySource<?> propertySource = event.getKind() == PropertySourceChangedEvent.Kind.REMOVED
                    ? event.getOldPropertySource()
                    : event.getNewPropertySource();
            if (propertySource == null) {
                continue;
            }
            if (propertySource instanceof org.springframework.core.env.EnumerablePropertySource) {
                org.springframework.core.env.EnumerablePropertySource<?> enumerable =
                        (org.springframework.core.env.EnumerablePropertySource<?>) propertySource;
                for (String name : enumerable.getPropertyNames()) {
                    if (!properties.containsKey(name)) {
                        properties.put(name, enumerable.getProperty(name));
                    }
                }
            }
        }
        return Collections.unmodifiableMap(properties);
    }

    private boolean contains(PropertySourceChangedEvent.Kind[] kinds, PropertySourceChangedEvent.Kind kind) {
        for (PropertySourceChangedEvent.Kind candidate : kinds) {
            if (candidate == kind) {
                return true;
            }
        }
        return false;
    }

    private Set<String> computeChangedKeys() {
        LinkedHashSet<String> keys = new LinkedHashSet<String>();
        for (PropertySourceChangedEvent sub : subEvents) {
            switch (sub.getKind()) {
                case ADDED:
                    keys.addAll(PropertySourceDiffSupport.keysAdded(sub.getNewPropertySource()));
                    break;
                case REMOVED:
                    keys.addAll(PropertySourceDiffSupport.keysRemoved(sub.getOldPropertySource()));
                    break;
                case REPLACED:
                    keys.addAll(PropertySourceDiffSupport.diffReplaced(
                            sub.getOldPropertySource(), sub.getNewPropertySource()));
                    break;
                default:
                    break;
            }
        }
        return Collections.unmodifiableSet(keys);
    }
}
