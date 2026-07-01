package io.zhijun.spring.config.env.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.zhijun.spring.config.env.event.PropertySourceChangedEvent.Kind.ADDED;
import static io.zhijun.spring.config.env.event.PropertySourceChangedEvent.Kind.REMOVED;
import static io.zhijun.spring.config.env.event.PropertySourceChangedEvent.Kind.REPLACED;

/**
 * Event raised when a group of property source changes is published.
 */
public class PropertySourcesChangedEvent extends ApplicationContextEvent {

    private final List<PropertySourceChangedEvent> subEvents;

    public PropertySourcesChangedEvent(ApplicationContext source, PropertySourceChangedEvent... subEvents) {
        this(source, asList(subEvents));
    }

    public PropertySourcesChangedEvent(ApplicationContext source, List<PropertySourceChangedEvent> subEvents) {
        super(source);
        this.subEvents = new ArrayList<PropertySourceChangedEvent>(subEvents);
    }

    public List<PropertySourceChangedEvent> getSubEvents() {
        return Collections.unmodifiableList(subEvents);
    }

    public Map<String, Object> getChangedProperties() {
        return getProperties(true, ADDED, REPLACED);
    }

    public Map<String, Object> getAddedProperties() {
        return getProperties(true, ADDED);
    }

    public Map<String, Object> getRemovedProperties() {
        return getProperties(false, REMOVED);
    }

    private Map<String, Object> getProperties(boolean useNew, PropertySourceChangedEvent.Kind... kinds) {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        for (PropertySourceChangedEvent subEvent : subEvents) {
            if (!contains(kinds, subEvent.getKind())) {
                continue;
            }
            PropertySource<?> propertySource = useNew ? subEvent.getNewPropertySource() : subEvent.getOldPropertySource();
            if (!(propertySource instanceof EnumerablePropertySource)) {
                continue;
            }
            EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) propertySource;
            for (String name : enumerable.getPropertyNames()) {
                if (!properties.containsKey(name)) {
                    properties.put(name, enumerable.getProperty(name));
                }
            }
        }
        return Collections.unmodifiableMap(properties);
    }

    private static boolean contains(PropertySourceChangedEvent.Kind[] kinds, PropertySourceChangedEvent.Kind target) {
        for (PropertySourceChangedEvent.Kind kind : kinds) {
            if (kind == target) {
                return true;
            }
        }
        return false;
    }

    private static List<PropertySourceChangedEvent> asList(PropertySourceChangedEvent[] events) {
        List<PropertySourceChangedEvent> list = new ArrayList<PropertySourceChangedEvent>(events.length);
        Collections.addAll(list, events);
        return list;
    }
}
