package io.zhijun.spring.config.context.annotation;

import io.zhijun.spring.config.env.event.PropertySourceChangedEvent;
import io.zhijun.spring.config.env.event.PropertySourcesChangedEvent;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base loader for annotations meta-annotated with {@link PropertySourceExtension}.
 */
public abstract class PropertySourceExtensionLoader<A extends Annotation, EA extends PropertySourceExtensionAttributes<A>>
        extends AnnotatedPropertySourceLoader<A> {

    private final Class<EA> extensionAttributesType;

    @SuppressWarnings("unchecked")
    protected PropertySourceExtensionLoader() {
        ResolvableType type = ResolvableType.forType(getClass()).as(PropertySourceExtensionLoader.class);
        this.extensionAttributesType = (Class<EA>) type.resolveGeneric(1);
    }

    public final Class<EA> getExtensionAttributesType() {
        return extensionAttributesType;
    }

    public abstract boolean isResourcePattern(String resourceValue);

    @Override
    protected final void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                            String propertySourceName, MutablePropertySources propertySources) throws Throwable {
        EA extensionAttributes = buildExtensionAttributes(getAnnotationType(), getExtensionAttributesType(),
                attributes, getEnvironment());
        PropertySource<?> propertySource = loadPropertySource(extensionAttributes, propertySourceName);
        if (propertySource == null) {
            String message = "No PropertySource resources found for @" + getAnnotationType().getName()
                    + " on " + metadata.getClassName();
            if (extensionAttributes.isIgnoreResourceNotFound()) {
                logger.warn(message);
            } else {
                throw new IllegalArgumentException(message);
            }
            return;
        }
        addPropertySource(extensionAttributes, propertySources, propertySource);
    }

    protected void addPropertySource(EA extensionAttributes, MutablePropertySources propertySources,
                                     PropertySource<?> propertySource) {
        if (extensionAttributes.isFirstPropertySource()) {
            propertySources.addFirst(propertySource);
            return;
        }
        String after = extensionAttributes.getAfterPropertySourceName();
        if (StringUtils.hasText(after)) {
            propertySources.addAfter(after, propertySource);
            return;
        }
        String before = extensionAttributes.getBeforePropertySourceName();
        if (StringUtils.hasText(before)) {
            propertySources.addBefore(before, propertySource);
            return;
        }
        propertySources.addLast(propertySource);
    }

    protected EA buildExtensionAttributes(Class<A> annotationType, Class<EA> extensionAttributesType,
                                          AnnotationAttributes attributes, ConfigurableEnvironment environment) throws Throwable {
        Constructor<EA> constructor = extensionAttributesType.getConstructor(Map.class, Class.class, PropertyResolver.class);
        return constructor.newInstance(attributes, annotationType, environment);
    }

    protected final PropertySource<?> loadPropertySource(EA extensionAttributes, String propertySourceName) throws Throwable {
        Comparator<Resource> comparator = createResourceComparator(extensionAttributes, propertySourceName);
        List<PropertySourceResource> resources = resolvePropertySourceResources(extensionAttributes, propertySourceName, comparator);
        if (resources.isEmpty()) {
            return null;
        }
        PropertySourceFactory factory = createPropertySourceFactory(extensionAttributes);
        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);
        for (int i = resources.size() - 1; i >= 0; i--) {
            PropertySourceResource propertySourceResource = resources.get(i);
            compositePropertySource.addPropertySource(
                    createResourcePropertySource(extensionAttributes, propertySourceName, factory, propertySourceResource));
        }
        if (extensionAttributes.isAutoRefreshed()) {
            configureResourcePropertySourcesRefresher(extensionAttributes, resources, compositePropertySource,
                    createResourcePropertySourcesRefresher(extensionAttributes, propertySourceName, factory, comparator));
        }
        return compositePropertySource;
    }

    private ResourcePropertySourcesRefresher createResourcePropertySourcesRefresher(EA extensionAttributes,
                                                                                    String propertySourceName,
                                                                                    PropertySourceFactory factory,
                                                                                    Comparator<Resource> comparator) {
        return (resourceValue, resource) -> {
            synchronized (this) {
                CompositePropertySource compositePropertySource = getPropertySource(propertySourceName);
                if (compositePropertySource == null) {
                    return;
                }
                List<PropertySourceChangedEvent> subEvents = new ArrayList<PropertySourceChangedEvent>();
                if (resource == null) {
                    refreshPropertySources(extensionAttributes, propertySourceName, factory, comparator, resourceValue,
                            compositePropertySource, subEvents);
                } else {
                    refreshPropertySources(extensionAttributes, propertySourceName, factory, comparator, resourceValue,
                            resource, compositePropertySource, subEvents);
                }
                if (!subEvents.isEmpty()) {
                    publishPropertySourcesChangedEvent(subEvents);
                }
            }
        };
    }

    private void publishPropertySourcesChangedEvent(List<PropertySourceChangedEvent> subEvents) {
        ConfigurableApplicationContext context = getApplicationContext();
        context.publishEvent(new PropertySourcesChangedEvent(context, subEvents));
    }

    private void refreshPropertySources(EA extensionAttributes, String propertySourceName, PropertySourceFactory factory,
                                        Comparator<Resource> comparator, String resourceValue,
                                        CompositePropertySource compositePropertySource,
                                        List<PropertySourceChangedEvent> subEvents) throws Throwable {
        List<PropertySourceResource> resources = resolvePropertySourceResources(extensionAttributes, propertySourceName,
                resourceValue, comparator);
        if (resources.isEmpty()) {
            return;
        }
        List<ResourceBackedPropertySource> oldPropertySources = getResourcePropertySources(compositePropertySource);
        List<ResourceBackedPropertySource> newPropertySources = new ArrayList<ResourceBackedPropertySource>(resources.size());
        for (PropertySourceResource resource : resources) {
            newPropertySources.add(createResourcePropertySource(extensionAttributes, propertySourceName, factory, resource));
        }
        updateResourcePropertySources(newPropertySources, oldPropertySources, subEvents);
        updatePropertySources(propertySourceName, oldPropertySources);
    }

    private void refreshPropertySources(EA extensionAttributes, String propertySourceName, PropertySourceFactory factory,
                                        Comparator<Resource> comparator, String resourceValue, Resource resource,
                                        CompositePropertySource compositePropertySource,
                                        List<PropertySourceChangedEvent> subEvents) throws Throwable {
        List<ResourceBackedPropertySource> propertySources = getResourcePropertySources(compositePropertySource);
        if (resource.exists()) {
            PropertySourceResource propertySourceResource = createPropertySourceResource(resourceValue, resource, comparator);
            ResourceBackedPropertySource newPropertySource = createResourcePropertySource(extensionAttributes, propertySourceName,
                    factory, propertySourceResource);
            updateResourcePropertySources(Collections.singletonList(newPropertySource), propertySources, subEvents);
        } else {
            removeResourcePropertySource(propertySourceName, resourceValue, resource, propertySources, subEvents);
        }
        updatePropertySources(propertySourceName, propertySources);
    }

    private void removeResourcePropertySource(String propertySourceName, String resourceValue, Resource resource,
                                              List<ResourceBackedPropertySource> propertySources,
                                              List<PropertySourceChangedEvent> subEvents) {
        String removedName = createResourcePropertySourceName(propertySourceName, resourceValue, resource);
        Iterator<ResourceBackedPropertySource> iterator = propertySources.iterator();
        while (iterator.hasNext()) {
            ResourceBackedPropertySource propertySource = iterator.next();
            if (removedName.equals(propertySource.getName())) {
                iterator.remove();
                subEvents.add(PropertySourceChangedEvent.removed(getApplicationContext(), propertySource));
            }
        }
    }

    private void updatePropertySources(String propertySourceName, List<ResourceBackedPropertySource> propertySources) {
        Collections.sort(propertySources);
        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);
        for (int i = propertySources.size() - 1; i >= 0; i--) {
            compositePropertySource.addPropertySource(propertySources.get(i));
        }
        getEnvironment().getPropertySources().replace(propertySourceName, compositePropertySource);
    }

    private void updateResourcePropertySources(Iterable<ResourceBackedPropertySource> newPropertySources,
                                               List<ResourceBackedPropertySource> existingPropertySources,
                                               List<PropertySourceChangedEvent> subEvents) {
        ConfigurableApplicationContext context = getApplicationContext();
        for (ResourceBackedPropertySource newPropertySource : newPropertySources) {
            String newName = normalizeChangeKey(newPropertySource.getName());
            boolean replaced = false;
            Iterator<ResourceBackedPropertySource> iterator = existingPropertySources.iterator();
            while (iterator.hasNext()) {
                ResourceBackedPropertySource oldPropertySource = iterator.next();
                if (newName.equals(normalizeChangeKey(oldPropertySource.getName()))
                        && newPropertySource.getName().equals(oldPropertySource.getName())) {
                    iterator.remove();
                    subEvents.add(PropertySourceChangedEvent.replaced(context, newPropertySource, oldPropertySource));
                    replaced = true;
                }
            }
            if (!replaced) {
                subEvents.add(PropertySourceChangedEvent.added(context, newPropertySource));
            }
            existingPropertySources.add(newPropertySource);
        }
    }

    private String normalizeChangeKey(String resourcePropertySourceName) {
        Assert.notNull(resourcePropertySourceName, "resourcePropertySourceName must not be null");
        return resourcePropertySourceName.substring(0, resourcePropertySourceName.lastIndexOf("@"));
    }

    private List<ResourceBackedPropertySource> getResourcePropertySources(CompositePropertySource compositePropertySource) {
        Collection<PropertySource<?>> propertySources = compositePropertySource.getPropertySources();
        List<ResourceBackedPropertySource> result = new ArrayList<ResourceBackedPropertySource>(propertySources.size());
        for (PropertySource<?> propertySource : propertySources) {
            result.add((ResourceBackedPropertySource) propertySource);
        }
        return result;
    }

    @Nullable
    private CompositePropertySource getPropertySource(String propertySourceName) {
        PropertySource<?> propertySource = getEnvironment().getPropertySources().get(propertySourceName);
        if (propertySource instanceof CompositePropertySource) {
            return (CompositePropertySource) propertySource;
        }
        return null;
    }

    private List<PropertySourceResource> resolvePropertySourceResources(EA extensionAttributes, String propertySourceName,
                                                                        Comparator<Resource> comparator) throws Throwable {
        String[] values = extensionAttributes.getValue();
        if (values.length == 0) {
            if (extensionAttributes.isIgnoreResourceNotFound()) {
                return Collections.emptyList();
            }
            throw new IllegalArgumentException("The 'value' attribute must be present at @" + getAnnotationType().getName());
        }
        List<PropertySourceResource> result = new ArrayList<PropertySourceResource>();
        for (String value : values) {
            result.addAll(resolvePropertySourceResources(extensionAttributes, propertySourceName, value, comparator));
        }
        Collections.sort(result);
        return result;
    }

    protected List<PropertySourceResource> resolvePropertySourceResources(EA extensionAttributes, String propertySourceName,
                                                                          String resourceValue,
                                                                          Comparator<Resource> comparator) throws Throwable {
        Resource[] resources = null;
        try {
            resources = resolveResources(extensionAttributes, propertySourceName, resourceValue);
        } catch (Throwable ex) {
            if (!extensionAttributes.isIgnoreResourceNotFound()) {
                throw ex;
            }
        }
        if (resources == null) {
            return Collections.emptyList();
        }
        List<PropertySourceResource> result = new ArrayList<PropertySourceResource>(resources.length);
        for (Resource resource : resources) {
            result.add(createPropertySourceResource(resourceValue, resource, comparator));
        }
        return result;
    }

    protected PropertySourceFactory createPropertySourceFactory(EA extensionAttributes) {
        return createInstance(extensionAttributes, PropertySourceExtensionAttributes::getPropertySourceFactoryClass);
    }

    protected Comparator<Resource> createResourceComparator(EA extensionAttributes, String propertySourceName) {
        return createInstance(extensionAttributes, PropertySourceExtensionAttributes::getResourceComparatorClass);
    }

    private PropertySourceResource createPropertySourceResource(String resourceValue, Resource resource,
                                                                Comparator<Resource> comparator) {
        return new PropertySourceResource(resourceValue, resource, comparator);
    }

    protected String createResourcePropertySourceName(String propertySourceName, String resourceValue, Resource resource) {
        Object suffix = resource.toString() == null ? resource.hashCode() : resource.toString();
        return propertySourceName + "#" + resourceValue + "@" + suffix;
    }

    protected final ResourceBackedPropertySource createResourcePropertySource(EA extensionAttributes,
                                                                              String propertySourceName,
                                                                              PropertySourceFactory factory,
                                                                              PropertySourceResource propertySourceResource) throws Throwable {
        Resource resource = propertySourceResource.resource;
        EncodedResource encodedResource = new EncodedResource(resource, extensionAttributes.getEncoding());
        String name = createResourcePropertySourceName(propertySourceName, propertySourceResource.resourceValue, resource);
        PropertySource<?> propertySource = factory.createPropertySource(name, encodedResource);
        return new ResourceBackedPropertySource(propertySourceResource, propertySource);
    }

    protected void configureResourcePropertySourcesRefresher(EA extensionAttributes,
                                                             List<PropertySourceResource> propertySourceResources,
                                                             CompositePropertySource propertySource,
                                                             ResourcePropertySourcesRefresher refresher) throws Throwable {
    }

    protected abstract Resource[] resolveResources(EA extensionAttributes, String propertySourceName,
                                                   String resourceValue) throws Throwable;

    protected <T> T createInstance(EA extensionAttributes, Function<EA, Class<T>> typeResolver) {
        Class<T> type = typeResolver.apply(extensionAttributes);
        return org.springframework.beans.BeanUtils.instantiateClass(type);
    }

    protected static class PropertySourceResource implements Comparable<PropertySourceResource> {

        private final String resourceValue;
        private final Resource resource;
        private final Comparator<Resource> resourceComparator;

        public PropertySourceResource(String resourceValue, Resource resource, Comparator<Resource> resourceComparator) {
            this.resourceValue = resourceValue;
            this.resource = resource;
            this.resourceComparator = resourceComparator;
        }

        public String getResourceValue() {
            return resourceValue;
        }

        public Resource getResource() {
            return resource;
        }

        @Override
        public int compareTo(PropertySourceResource other) {
            return resourceComparator.compare(resource, other.resource);
        }
    }

    protected static class ResourceBackedPropertySource<T> extends EnumerablePropertySource<T>
            implements Comparable<ResourceBackedPropertySource<T>> {

        private final PropertySourceResource propertySourceResource;
        private final PropertySource<T> original;
        private final EnumerablePropertySource<T> enumerablePropertySource;

        @SuppressWarnings("unchecked")
        public ResourceBackedPropertySource(PropertySourceResource propertySourceResource, PropertySource<T> original) {
            super(original.getName(), original.getSource());
            this.propertySourceResource = propertySourceResource;
            this.original = original;
            this.enumerablePropertySource = original instanceof EnumerablePropertySource
                    ? (EnumerablePropertySource<T>) original : null;
        }

        @Override
        public String[] getPropertyNames() {
            return enumerablePropertySource != null ? enumerablePropertySource.getPropertyNames() : new String[0];
        }

        @Override
        public Object getProperty(String name) {
            return enumerablePropertySource != null ? enumerablePropertySource.getProperty(name) : null;
        }

        public String getResourceValue() {
            return propertySourceResource.resourceValue;
        }

        public Resource getResource() {
            return propertySourceResource.resource;
        }

        public PropertySource<T> getOriginal() {
            return original;
        }

        @Override
        public int compareTo(ResourceBackedPropertySource<T> other) {
            return propertySourceResource.compareTo(other.propertySourceResource);
        }
    }

    @FunctionalInterface
    protected interface ResourcePropertySourcesRefresher {
        void refresh(String resourceValue, @Nullable Resource resource) throws Throwable;

        default void refresh(String resourceValue) throws Throwable {
            refresh(resourceValue, null);
        }
    }
}
