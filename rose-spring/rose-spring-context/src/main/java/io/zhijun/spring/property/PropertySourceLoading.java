package io.zhijun.spring.property;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.StringUtils;


/**
 * Shared loading logic for {@code @ResourcePropertySource}.
 */
final class PropertySourceLoading {

    private PropertySourceLoading() {}

    static void loadPropertySource(
            AnnotatedPropertySourceImportSelector<?> context,
            Class<?> importingClass,
            AnnotationAttributes attributes,
            Class<? extends Annotation> annotationType) {
        try {
            String propertySourceName = resolvePropertySourceName(importingClass, attributes, annotationType);
            List<Resource> resources = resolveResources(context, attributes);
            if (resources.isEmpty()) {
                return;
            }
            MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
            PropertySource<?> propertySource =
                    createCompositePropertySource(context, propertySourceName, attributes, resources);
            addPropertySource(attributes, propertySources, propertySource);
            registerAutoRefreshIfNeeded(context, propertySourceName, attributes, annotationType);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load property source for " + importingClass.getName(), ex);
        }
    }

    static void copyContextFrom(
            AnnotatedPropertySourceImportSelector<?> target, AnnotatedPropertySourceImportSelector<?> source) {
        target.setEnvironment(source.getEnvironment());
        target.setBeanClassLoader(source.getClassLoader());
        target.setResourceLoader(source.getResourceLoader());
    }

    private static PropertySource<?> createCompositePropertySource(
            AnnotatedPropertySourceImportSelector<?> context,
            String propertySourceName,
            AnnotationAttributes attributes,
            List<Resource> resources)
            throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends PropertySourceFactory> factoryType =
            attributes.getClass("factory");
        PropertySourceFactory factory = BeanUtils.instantiateClass(factoryType);
        if (resources.size() == 1) {
            Resource resource = resources.get(0);
            String sourceName = propertySourceName + "@" + resource.getDescription();
            return factory.createPropertySource(
                    sourceName, new EncodedResource(resource, resolveEncoding(context, attributes)));
        }
        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);
        for (Resource resource : resources) {
            String sourceName = propertySourceName + "@" + resource.getDescription();
            PropertySource<?> propertySource = factory.createPropertySource(
                    sourceName, new EncodedResource(resource, resolveEncoding(context, attributes)));
            compositePropertySource.addPropertySource(propertySource);
        }
        return compositePropertySource;
    }

    private static List<Resource> resolveResources(
            AnnotatedPropertySourceImportSelector<?> context, AnnotationAttributes attributes) {
        String[] locations = attributes.getStringArray("value");
        if (locations == null || locations.length < 1) {
            return Collections.emptyList();
        }
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver(context.getResourceLoader());
        List<Resource> resources = new ArrayList<Resource>();
        try {
            for (String location : locations) {
                String resolvedLocation = resolvePlaceholders(context, location);
                Resource[] resolved = resolver.getResources(resolvedLocation);
                if (resolved.length == 0 && !attributes.getBoolean("ignoreResourceNotFound")) {
                    throw new IllegalStateException("Resource location [" + resolvedLocation + "] not found");
                }
                Collections.addAll(resources, resolved);
            }
            @SuppressWarnings("unchecked")
            Class<? extends Comparator<Resource>> comparatorType =
                attributes.getClass("resourceComparator");
            sortResources(resources, comparatorType);
       } catch (Exception ex) {
            throw new IllegalStateException("Failed to resolve resources", ex);
        }
        return resources;
    }

    private static String resolvePropertySourceName(
            Class<?> importingClass, AnnotationAttributes attributes, Class<? extends Annotation> annotationType) {
        String name = attributes.getString("name");
        if (StringUtils.hasText(name)) {
            return name;
        }
        return importingClass.getName() + "@" + annotationType.getName();
    }

    private static void addPropertySource(
            AnnotationAttributes attributes, MutablePropertySources propertySources, PropertySource<?> propertySource) {
        if (attributes.getBoolean("first")) {
            propertySources.addFirst(propertySource);
            return;
        }
        String after = attributes.getString("after");
        if (StringUtils.hasText(after)) {
            propertySources.addAfter(after, propertySource);
            return;
        }
        String before = attributes.getString("before");
        if (StringUtils.hasText(before)) {
            propertySources.addBefore(before, propertySource);
            return;
        }
        propertySources.addLast(propertySource);
    }

    private static void registerAutoRefreshIfNeeded(
            final AnnotatedPropertySourceImportSelector<?> context,
            final String propertySourceName,
            final AnnotationAttributes attributes,
            final Class<? extends Annotation> annotationType) {
        if (!attributes.getBoolean("autoRefreshed")) {
            return;
        }
        try {
            final AutoRefreshWatcher watcher = new AutoRefreshWatcher();
            String[] locations = attributes.getStringArray("value");
            if (locations != null) {
                for (String location : locations) {
                    if (StringUtils.hasText(location)) {
                        watcher.watch(resolvePlaceholders(context, location), (resourceValue, resource) -> {
                    try {
                        reload(context, propertySourceName, attributes, annotationType);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                    }
                }
            }
            watcher.start();
            AutoRefreshWatcherLifecycle.register(watcher);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to enable auto-refresh for " + propertySourceName, ex);
        }
    }

    private static void reload(
            AnnotatedPropertySourceImportSelector<?> context,
            String propertySourceName,
            AnnotationAttributes attributes,
            Class<? extends Annotation> annotationType)
            throws Exception {
        ConfigurableEnvironment environment = context.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        List<Resource> resources = resolveResources(context, attributes);
        if (resources.isEmpty()) {
            if (propertySources.contains(propertySourceName)) {
                propertySources.replace(
                        propertySourceName,
                        new MapPropertySource(propertySourceName, Collections.emptyMap()));
            }
            return;
        }
        PropertySource<?> refreshed = createCompositePropertySource(context, propertySourceName, attributes, resources);
        if (propertySources.contains(propertySourceName)) {
            propertySources.replace(propertySourceName, refreshed);
        } else {
            addPropertySource(attributes, propertySources, refreshed);
        }
    }

    private static String resolvePlaceholders(AnnotatedPropertySourceImportSelector<?> context, String value) {
        return context.getEnvironment().resolvePlaceholders(value);
    }

    private static String resolveEncoding(
            AnnotatedPropertySourceImportSelector<?> context, AnnotationAttributes attributes) {
        return resolvePlaceholders(context, attributes.getString("encoding"));
    }

    private static void sortResources(List<Resource> resources, Class<? extends Comparator<Resource>> comparatorType)
            throws Exception {
        if (resources.size() < 2) {
            return;
        }
        Comparator<Resource> comparator = BeanUtils.instantiateClass(comparatorType);
        Collections.sort(resources, comparator);
    }
}
