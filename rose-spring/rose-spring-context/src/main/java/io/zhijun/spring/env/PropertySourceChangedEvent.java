package io.zhijun.spring.env;

import java.util.Objects;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * Property source change event.
 */
public class PropertySourceChangedEvent extends ApplicationContextEvent {

    public enum Kind {
        ADDED,
        REPLACED,
        REMOVED
    }

    private final Kind kind;

    private final PropertySource<?> newPropertySource;

    private final PropertySource<?> oldPropertySource;

    protected PropertySourceChangedEvent(
            ApplicationContext source,
            Kind kind,
            PropertySource<?> newPropertySource,
            PropertySource<?> oldPropertySource) {
        super(source);
        this.kind = kind;
        this.newPropertySource = newPropertySource;
        this.oldPropertySource = oldPropertySource;
    }

    public static PropertySourceChangedEvent added(ApplicationContext source, PropertySource<?> newPropertySource) {
        Assert.notNull(newPropertySource, "newPropertySource cannot be null");
        return new PropertySourceChangedEvent(source, Kind.ADDED, newPropertySource, null);
    }

    public static PropertySourceChangedEvent replaced(
            ApplicationContext source, PropertySource<?> newPropertySource, PropertySource<?> oldPropertySource) {
        Assert.notNull(newPropertySource, "newPropertySource cannot be null");
        Assert.notNull(oldPropertySource, "oldPropertySource cannot be null");
        return new PropertySourceChangedEvent(source, Kind.REPLACED, newPropertySource, oldPropertySource);
    }

    public static PropertySourceChangedEvent removed(ApplicationContext source, PropertySource<?> oldPropertySource) {
        Assert.notNull(oldPropertySource, "oldPropertySource cannot be null");
        return new PropertySourceChangedEvent(source, Kind.REMOVED, null, oldPropertySource);
    }

    public Kind getKind() {
        return kind;
    }

    public PropertySource<?> getNewPropertySource() {
        return newPropertySource;
    }

    public PropertySource<?> getOldPropertySource() {
        return oldPropertySource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertySourceChangedEvent)) {
            return false;
        }
        PropertySourceChangedEvent that = (PropertySourceChangedEvent) o;
        return kind == that.kind
                && Objects.equals(newPropertySource, that.newPropertySource)
                && Objects.equals(oldPropertySource, that.oldPropertySource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, newPropertySource, oldPropertySource);
    }
}
