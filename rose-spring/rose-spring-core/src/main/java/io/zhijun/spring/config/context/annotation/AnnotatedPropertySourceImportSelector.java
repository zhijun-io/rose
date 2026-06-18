package io.zhijun.spring.config.context.annotation;

import java.lang.annotation.Annotation;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Lightweight {@link ImportSelector} base for annotation-driven property source loading.
 */
abstract class AnnotatedPropertySourceImportSelector<A extends Annotation> implements ImportSelector,
        EnvironmentAware, BeanClassLoaderAware, ResourceLoaderAware {

    private static final String[] NO_IMPORTS = new String[0];

    private final Class<A> annotationType;

    private ConfigurableEnvironment environment;

    private ClassLoader classLoader;

    private ResourceLoader resourceLoader;

    AnnotatedPropertySourceImportSelector(Class<A> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public final String[] selectImports(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = resolveAnnotationAttributes(metadata);
        if (attributes != null) {
            loadPropertySource(attributes, metadata);
        }
        return NO_IMPORTS;
    }

    protected abstract void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata);

    protected final Class<A> getAnnotationType() {
        return annotationType;
    }

    protected final ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    protected final ClassLoader getClassLoader() {
        return classLoader == null ? getClass().getClassLoader() : classLoader;
    }

    protected final ResourceLoader getResourceLoader() {
        return resourceLoader == null ? new org.springframework.core.io.DefaultResourceLoader(getClassLoader())
                : resourceLoader;
    }

    protected final Class<?> resolveImportingClass(AnnotationMetadata metadata) {
        try {
            return Class.forName(metadata.getClassName(), false, getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private AnnotationAttributes resolveAnnotationAttributes(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(annotationType.getName()));
        if (attributes != null) {
            return attributes;
        }
        Class<?> importingClass = resolveImportingClass(metadata);
        A annotation = AnnotatedElementUtils.findMergedAnnotation(importingClass, annotationType);
        if (annotation == null) {
            return null;
        }
        return AnnotationAttributes.fromMap(AnnotationUtils.getAnnotationAttributes(annotation));
    }
}
