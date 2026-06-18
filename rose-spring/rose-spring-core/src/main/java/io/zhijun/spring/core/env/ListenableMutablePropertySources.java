package io.zhijun.spring.core.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import io.zhijun.spring.config.env.event.PropertySourceChangedEvent;
import io.zhijun.spring.config.env.event.PropertySourcesChangedEvent;

/**
 * MutablePropertySources wrapper that publishes change callbacks.
 */
public class ListenableMutablePropertySources extends MutablePropertySources {

    private final MutablePropertySources delegate;

    private final ApplicationContext applicationContext;

    private final List<EnvironmentListener> listeners;

    public ListenableMutablePropertySources(MutablePropertySources delegate, ApplicationContext applicationContext,
            List<EnvironmentListener> listeners) {
        this.delegate = delegate;
        this.applicationContext = applicationContext;
        this.listeners = listeners == null ? Collections.emptyList() : new ArrayList<EnvironmentListener>(listeners);
    }

    @Override
    public void addFirst(PropertySource<?> propertySource) {
        delegate.addFirst(propertySource);
        publish(PropertySourceChangedEvent.added(applicationContext, propertySource));
    }

    @Override
    public void addLast(PropertySource<?> propertySource) {
        delegate.addLast(propertySource);
        publish(PropertySourceChangedEvent.added(applicationContext, propertySource));
    }

    @Override
    public void addBefore(String relativePropertySourceName, PropertySource<?> propertySource) {
        delegate.addBefore(relativePropertySourceName, propertySource);
        publish(PropertySourceChangedEvent.added(applicationContext, propertySource));
    }

    @Override
    public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
        delegate.addAfter(relativePropertySourceName, propertySource);
        publish(PropertySourceChangedEvent.added(applicationContext, propertySource));
    }

    @Override
    public Iterator<PropertySource<?>> iterator() {
        return delegate.iterator();
    }

    @Override
    public Spliterator<PropertySource<?>> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public Stream<PropertySource<?>> stream() {
        return delegate.stream();
    }

    @Override
    public boolean contains(String name) {
        return delegate.contains(name);
    }

    @Override
    public PropertySource<?> get(String name) {
        return delegate.get(name);
    }

    @Override
    public int precedenceOf(PropertySource<?> propertySource) {
        return delegate.precedenceOf(propertySource);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public PropertySource<?> remove(String name) {
        PropertySource<?> removed = delegate.remove(name);
        if (removed != null) {
            publish(PropertySourceChangedEvent.removed(applicationContext, removed));
        }
        return removed;
    }

    @Override
    public void replace(String name, PropertySource<?> propertySource) {
        PropertySource<?> old = delegate.get(name);
        delegate.replace(name, propertySource);
        if (old != null) {
            publish(PropertySourceChangedEvent.replaced(applicationContext, propertySource, old));
        }
        else {
            publish(PropertySourceChangedEvent.added(applicationContext, propertySource));
        }
    }

    private void publish(PropertySourceChangedEvent event) {
        PropertySourcesChangedEvent bulkEvent = new PropertySourcesChangedEvent(applicationContext,
                Collections.singletonList(event));
        for (EnvironmentListener listener : listeners) {
            listener.onPropertySourceChanged(event);
            listener.onPropertySourcesChanged(bulkEvent);
        }
    }
}
