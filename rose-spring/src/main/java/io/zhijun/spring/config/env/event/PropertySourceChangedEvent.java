package io.zhijun.spring.config.env.event;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * Event raised when a property source changes.
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

    protected PropertySourceChangedEvent(ApplicationContext source, Kind kind,
                                         @Nullable PropertySource<?> newPropertySource,
                                         @Nullable PropertySource<?> oldPropertySource) {
        super(source);
        this.kind = kind;
        this.newPropertySource = newPropertySource;
        this.oldPropertySource = oldPropertySource;
    }

    protected PropertySourceChangedEvent(ApplicationContext source, Kind kind, @Nullable PropertySource<?> newPropertySource) {
        this(source, kind, newPropertySource, null);
    }

    public Kind getKind() {
        return kind;
    }

    @Nullable
    public PropertySource<?> getNewPropertySource() {
        return newPropertySource;
    }

    @Nullable
    public PropertySource<?> getOldPropertySource() {
        return oldPropertySource;
    }

    public static PropertySourceChangedEvent added(ApplicationContext source, PropertySource<?> newPropertySource) {
        Assert.notNull(newPropertySource, "newPropertySource must not be null");
        return new PropertySourceChangedEvent(source, Kind.ADDED, newPropertySource);
    }

    public static PropertySourceChangedEvent replaced(ApplicationContext source, PropertySource<?> newPropertySource,
                                                      PropertySource<?> oldPropertySource) {
        Assert.notNull(newPropertySource, "newPropertySource must not be null");
        Assert.notNull(oldPropertySource, "oldPropertySource must not be null");
        return new PropertySourceChangedEvent(source, Kind.REPLACED, newPropertySource, oldPropertySource);
    }

    public static PropertySourceChangedEvent removed(ApplicationContext source, PropertySource<?> oldPropertySource) {
        Assert.notNull(oldPropertySource, "oldPropertySource must not be null");
        return new PropertySourceChangedEvent(source, Kind.REMOVED, null, oldPropertySource);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PropertySourceChangedEvent)) {
            return false;
        }
        PropertySourceChangedEvent that = (PropertySourceChangedEvent) other;
        return kind == that.kind
                && Objects.equals(newPropertySource, that.newPropertySource)
                && Objects.equals(oldPropertySource, that.oldPropertySource);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(kind);
        result = 31 * result + Objects.hashCode(newPropertySource);
        result = 31 * result + Objects.hashCode(oldPropertySource);
        return result;
    }
}
