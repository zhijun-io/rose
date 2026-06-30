package io.zhijun.spring.propertysource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.zhijun.spring.propertysource.ResourcePropertySource;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

class PropertySourceLoadingTests {

    @Test
    @SuppressWarnings("unchecked")
    void throwsOnMissingResourceWhenNotFoundNotIgnored()
            throws Exception {
        AnnotatedPropertySourceImportSelector<?> context =
                mock(AnnotatedPropertySourceImportSelector.class);
        ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
        when(env.resolvePlaceholders(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(context.getEnvironment()).thenReturn(env);
        when(context.getResourceLoader()).thenReturn(mock(ResourceLoader.class));

        AnnotationAttributes attributes = new AnnotationAttributes();
        attributes.put("value", new String[] {"classpath*:/nonexistent.properties"});
        attributes.put("autoRefreshed", false);
        attributes.put("ignoreResourceNotFound", false);
        attributes.put("name", "");
        attributes.put("first", false);
        attributes.put("after", "");
        attributes.put("before", "");
        attributes.put("encoding", "");
        attributes.put("resourceComparator", DefaultResourceComparator.class);
        attributes.put("factory", DefaultPropertySourceFactory.class);

        assertThatThrownBy(() ->
                PropertySourceLoading.loadPropertySource(
                        context, getClass(), attributes, ResourcePropertySource.class))
                .isInstanceOf(IllegalStateException.class)
                ;
    }

    @Test
    @SuppressWarnings("unchecked")
    void skipsWhenResourcesEmpty()
            throws Exception {
        AnnotatedPropertySourceImportSelector<?> context =
                mock(AnnotatedPropertySourceImportSelector.class);
        ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
        when(context.getEnvironment()).thenReturn(env);
        when(context.getResourceLoader()).thenReturn(mock(ResourceLoader.class));

        AnnotationAttributes attributes = new AnnotationAttributes();
        attributes.put("value", new String[0]);
        attributes.put("autoRefreshed", false);
        attributes.put("ignoreResourceNotFound", false);
        attributes.put("name", "");
        attributes.put("first", false);
        attributes.put("after", "");
        attributes.put("before", "");
        attributes.put("encoding", "");
        attributes.put("resourceComparator", DefaultResourceComparator.class);
        attributes.put("factory", DefaultPropertySourceFactory.class);

        // Should not throw with empty locations
        PropertySourceLoading.loadPropertySource(
                context, getClass(), attributes, ResourcePropertySource.class);
    }

    @Test
    void copyContextFromDelegates() {
        AnnotatedPropertySourceImportSelector<?> target =
                mock(AnnotatedPropertySourceImportSelector.class);
        AnnotatedPropertySourceImportSelector<?> source =
                mock(AnnotatedPropertySourceImportSelector.class);
        ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
        ClassLoader classLoader = getClass().getClassLoader();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        when(source.getEnvironment()).thenReturn(env);
        when(source.getClassLoader()).thenReturn(classLoader);
        when(source.getResourceLoader()).thenReturn(resourceLoader);

        PropertySourceLoading.copyContextFrom(target, source);

        verify(target).setEnvironment(env);
        verify(target).setBeanClassLoader(classLoader);
        verify(target).setResourceLoader(resourceLoader);
    }

    // Factory used by the tests
    public static class DefaultPropertySourceFactory
            implements org.springframework.core.io.support.PropertySourceFactory {
        @Override
        public PropertySource<?> createPropertySource(
                String name, org.springframework.core.io.support.EncodedResource resource) {
            return new org.springframework.core.env.MapPropertySource(name, java.util.Collections.emptyMap());
        }
    }
}
