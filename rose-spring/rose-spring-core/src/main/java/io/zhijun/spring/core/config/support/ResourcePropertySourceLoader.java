package io.zhijun.spring.core.config.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.zhijun.spring.core.env.refresh.ResourcePropertySourceRefreshLifecycle;
import io.zhijun.spring.core.env.refresh.ResourcePropertySourceRefreshWatcher;
import io.zhijun.spring.core.env.refresh.ResourcePropertySourcesRefresher;
import io.zhijun.spring.core.config.annotation.ResourcePropertySource;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

/**
 * Loader for enhanced resource property sources.
 */
public class ResourcePropertySourceLoader extends AnnotatedPropertySourceImportSelector<ResourcePropertySource> {

    public ResourcePropertySourceLoader() {
        super(ResourcePropertySource.class);
    }

    @Override
    protected void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        Class<?> importingClass = resolveImportingClass(metadata);
        loadPropertySource(importingClass, attributes);
    }

    void loadPropertySource(Class<?> importingClass, AnnotationAttributes attributes) {
        ResourcePropertySource annotation = AnnotationUtils.synthesizeAnnotation(attributes,
                ResourcePropertySource.class, importingClass);
        try {
            String propertySourceName = resolvePropertySourceName(importingClass, annotation);
            List<Resource> resources = resolveResources(annotation);
            if (resources.isEmpty()) {
                return;
            }
            MutablePropertySources propertySources = getEnvironment().getPropertySources();
            PropertySource<?> propertySource = createCompositePropertySource(propertySourceName, annotation, resources);
            addPropertySource(annotation, propertySources, propertySource);
            registerAutoRefreshIfNeeded(propertySourceName, annotation);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load property source for " + importingClass.getName(), ex);
        }
    }

    void copyContextFrom(AnnotatedPropertySourceImportSelector<?> source) {
        setEnvironment(source.getEnvironment());
        setBeanClassLoader(source.getClassLoader());
        setResourceLoader(source.getResourceLoader());
    }

    private PropertySource<?> createCompositePropertySource(String propertySourceName,
            ResourcePropertySource annotation, List<Resource> resources) throws Exception {
        PropertySourceFactory factory = BeanUtils.instantiateClass(annotation.factory());
        if (resources.size() == 1) {
            Resource resource = resources.get(0);
            String sourceName = propertySourceName + "@" + resource.getDescription();
            return factory.createPropertySource(sourceName, new EncodedResource(resource, resolveEncoding(annotation)));
        }
        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);
        for (Resource resource : resources) {
            String sourceName = propertySourceName + "@" + resource.getDescription();
            PropertySource<?> propertySource = factory.createPropertySource(sourceName,
                    new EncodedResource(resource, resolveEncoding(annotation)));
            compositePropertySource.addPropertySource(propertySource);
        }
        return compositePropertySource;
    }

    private List<Resource> resolveResources(ResourcePropertySource annotation) {
        String[] locations = annotation.value();
        if (locations == null || locations.length < 1) {
            return Collections.emptyList();
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getResourceLoader());
        List<Resource> resources = new ArrayList<Resource>();
        try {
            for (String location : locations) {
                String resolvedLocation = resolvePlaceholders(location);
                Resource[] resolved = resolver.getResources(resolvedLocation);
                if (resolved.length == 0 && !annotation.ignoreResourceNotFound()) {
                    throw new IllegalStateException("Resource location [" + resolvedLocation + "] not found");
                }
                Collections.addAll(resources, resolved);
            }
            sortResources(resources, annotation.resourceComparator());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to resolve resources", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to resolve resources", ex);
        }
        return resources;
    }

    private String resolvePropertySourceName(Class<?> importingClass, ResourcePropertySource annotation) {
        if (StringUtils.hasText(annotation.name())) {
            return annotation.name();
        }
        return importingClass.getName() + "@" + ResourcePropertySource.class.getName();
    }

    private void addPropertySource(ResourcePropertySource annotation, MutablePropertySources propertySources,
            PropertySource<?> propertySource) {
        if (annotation.first()) {
            propertySources.addFirst(propertySource);
            return;
        }
        if (StringUtils.hasText(annotation.after())) {
            propertySources.addAfter(annotation.after(), propertySource);
            return;
        }
        if (StringUtils.hasText(annotation.before())) {
            propertySources.addBefore(annotation.before(), propertySource);
            return;
        }
        propertySources.addLast(propertySource);
    }

    private void registerAutoRefreshIfNeeded(final String propertySourceName, final ResourcePropertySource annotation) {
        if (!annotation.autoRefreshed()) {
            return;
        }
        try {
            final ResourcePropertySourceRefreshWatcher watcher = new ResourcePropertySourceRefreshWatcher();
            String[] locations = annotation.value();
            if (locations != null && locations.length > 0) {
                watcher.watch(resolvePlaceholders(locations[0]), new ResourcePropertySourcesRefresher() {
                    @Override
                    public void refresh(String resourceValue, Resource resource) throws Throwable {
                        reload(propertySourceName, annotation);
                    }
                });
            }
            ResourcePropertySourceRefreshLifecycle.register(watcher);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to enable auto-refresh for " + propertySourceName, ex);
        }
    }

    private void reload(String propertySourceName, ResourcePropertySource annotation) throws Exception {
        ConfigurableEnvironment environment = getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        List<Resource> resources = resolveResources(annotation);
        if (resources.isEmpty()) {
            if (propertySources.contains(propertySourceName)) {
                propertySources.replace(propertySourceName,
                        new MapPropertySource(propertySourceName, Collections.<String, Object>emptyMap()));
            }
            return;
        }
        PropertySource<?> refreshed = createCompositePropertySource(propertySourceName, annotation, resources);
        if (propertySources.contains(propertySourceName)) {
            propertySources.replace(propertySourceName, refreshed);
        } else {
            addPropertySource(annotation, propertySources, refreshed);
        }
    }

    private String resolvePlaceholders(String value) {
        return getEnvironment().resolvePlaceholders(value);
    }

    private String resolveEncoding(ResourcePropertySource annotation) {
        return resolvePlaceholders(annotation.encoding());
    }

    private void sortResources(List<Resource> resources, Class<? extends Comparator<Resource>> comparatorType)
            throws Exception {
        if (resources.size() < 2) {
            return;
        }
        Comparator<Resource> comparator = BeanUtils.instantiateClass(comparatorType);
        Collections.sort(resources, comparator);
    }
}
