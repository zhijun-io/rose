package io.zhijun.spring.core.env;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MissingRequiredPropertiesException;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import org.springframework.lang.Nullable;

import io.zhijun.spring.core.env.listener.EnvironmentListener;
import io.zhijun.spring.core.env.listener.ProfileListener;
import io.zhijun.spring.core.env.listener.PropertyResolverListener;
import io.zhijun.spring.core.io.support.SpringFactoriesLoaderUtils;

/**
 * ConfigurableEnvironment with intercepting features.
 */
public class ListenableConfigurableEnvironment implements ConfigurableEnvironment {

    @Nullable
    private static final MethodHandle SET_ESCAPE_CHARACTER_METHOD_HANDLE = null;

    private final ConfigurableEnvironment delegate;

    private final List<EnvironmentListener> environmentListeners;

    private final List<ProfileListener> profileListeners;

    private final List<PropertyResolverListener> propertyResolverListeners;

    private final MutablePropertySources propertySources;

    public ListenableConfigurableEnvironment(
            ConfigurableEnvironment delegate,
            ApplicationContext applicationContext,
            List<EnvironmentListener> listeners) {
        this.delegate = delegate;
        this.environmentListeners =
                listeners == null ? Collections.emptyList() : new ArrayList<EnvironmentListener>(listeners);
        this.profileListeners = loadProfileListeners(applicationContext, this.environmentListeners);
        this.propertyResolverListeners = loadPropertyResolverListeners(applicationContext, this.environmentListeners);
        this.propertySources = new ListenableMutablePropertySources(
                delegate.getPropertySources(), applicationContext, this.environmentListeners);
    }

    public ListenableConfigurableEnvironment(ConfigurableEnvironment delegate, ApplicationContext applicationContext) {
        this(
                delegate,
                applicationContext,
                SpringFactoriesLoaderUtils.loadFactories(
                        EnvironmentListener.class,
                        applicationContext == null ? null : applicationContext.getClassLoader()));
    }

    private List<ProfileListener> loadProfileListeners(
            ApplicationContext applicationContext, List<EnvironmentListener> environmentListeners) {
        List<ProfileListener> profileListeners = new ArrayList<ProfileListener>(environmentListeners);
        profileListeners.addAll(SpringFactoriesLoaderUtils.loadFactories(
                ProfileListener.class, applicationContext == null ? null : applicationContext.getClassLoader()));
        if (profileListeners.size() > 1) {
            Collections.sort(
                    profileListeners, org.springframework.core.annotation.AnnotationAwareOrderComparator.INSTANCE);
        }
        return profileListeners;
    }

    private List<PropertyResolverListener> loadPropertyResolverListeners(
            ApplicationContext applicationContext, List<EnvironmentListener> environmentListeners) {
        List<PropertyResolverListener> propertyResolverListeners =
                new ArrayList<PropertyResolverListener>(environmentListeners);
        propertyResolverListeners.addAll(SpringFactoriesLoaderUtils.loadFactories(
                PropertyResolverListener.class,
                applicationContext == null ? null : applicationContext.getClassLoader()));
        if (propertyResolverListeners.size() > 1) {
            Collections.sort(
                    propertyResolverListeners,
                    org.springframework.core.annotation.AnnotationAwareOrderComparator.INSTANCE);
        }
        return propertyResolverListeners;
    }

    @Override
    public void setActiveProfiles(String... profiles) {
        forEachProfileListener(listener -> listener.beforeSetActiveProfiles(delegate, profiles));
        delegate.setActiveProfiles(profiles);
        forEachProfileListener(listener -> listener.afterSetActiveProfiles(delegate, profiles));
    }

    @Override
    public void addActiveProfile(String profile) {
        forEachProfileListener(listener -> listener.beforeAddActiveProfile(delegate, profile));
        delegate.addActiveProfile(profile);
        forEachProfileListener(listener -> listener.afterAddActiveProfile(delegate, profile));
    }

    @Override
    public void setDefaultProfiles(String... profiles) {
        forEachProfileListener(listener -> listener.beforeSetDefaultProfiles(delegate, profiles));
        delegate.setDefaultProfiles(profiles);
        forEachProfileListener(listener -> listener.afterSetDefaultProfiles(delegate, profiles));
    }

    @Override
    public MutablePropertySources getPropertySources() {
        forEachEnvironmentListener(listener -> listener.beforeGetPropertySources(delegate));
        forEachEnvironmentListener(listener -> listener.afterGetPropertySources(delegate, propertySources));
        return propertySources;
    }

    @Override
    public Map<String, Object> getSystemProperties() {
        forEachEnvironmentListener(listener -> listener.beforeGetSystemProperties(delegate));
        Map<String, Object> systemProperties = delegate.getSystemProperties();
        forEachEnvironmentListener(listener -> listener.afterGetSystemProperties(delegate, systemProperties));
        return systemProperties;
    }

    @Override
    public Map<String, Object> getSystemEnvironment() {
        forEachEnvironmentListener(listener -> listener.beforeGetSystemEnvironment(delegate));
        Map<String, Object> systemEnvironment = delegate.getSystemEnvironment();
        forEachEnvironmentListener(listener -> listener.afterGetSystemEnvironment(delegate, systemEnvironment));
        return systemEnvironment;
    }

    @Override
    public void merge(ConfigurableEnvironment parent) {
        forEachEnvironmentListener(listener -> listener.beforeMerge(delegate, parent));
        delegate.merge(parent);
        forEachEnvironmentListener(listener -> listener.afterMerge(delegate, parent));
    }

    @Override
    public String[] getActiveProfiles() {
        forEachProfileListener(listener -> listener.beforeGetActiveProfiles(delegate));
        String[] activeProfiles = delegate.getActiveProfiles();
        forEachProfileListener(listener -> listener.afterGetActiveProfiles(delegate, activeProfiles));
        return activeProfiles;
    }

    @Override
    public String[] getDefaultProfiles() {
        forEachProfileListener(listener -> listener.beforeGetDefaultProfiles(delegate));
        String[] defaultProfiles = delegate.getDefaultProfiles();
        forEachProfileListener(listener -> listener.afterGetDefaultProfiles(delegate, defaultProfiles));
        return defaultProfiles;
    }

    @Override
    public boolean matchesProfiles(String... profileExpressions) {
        return delegate.matchesProfiles(profileExpressions);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean acceptsProfiles(String... profiles) {
        return delegate.acceptsProfiles(profiles);
    }

    @Override
    public boolean acceptsProfiles(Profiles profiles) {
        return delegate.acceptsProfiles(profiles);
    }

    @Override
    public boolean containsProperty(String key) {
        return delegate.containsProperty(key);
    }

    @Override
    public String getProperty(String key) {
        forEachPropertyResolverListener(listener -> listener.beforeGetProperty(delegate, key, String.class, null));
        String value = delegate.getProperty(key);
        forEachPropertyResolverListener(
                listener -> listener.afterGetProperty(delegate, key, String.class, value, null));
        return value;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        forEachPropertyResolverListener(
                listener -> listener.beforeGetProperty(delegate, key, String.class, defaultValue));
        String value = delegate.getProperty(key, defaultValue);
        forEachPropertyResolverListener(
                listener -> listener.afterGetProperty(delegate, key, String.class, value, defaultValue));
        return value;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        forEachPropertyResolverListener(listener -> listener.beforeGetProperty(delegate, key, targetType, null));
        T value = delegate.getProperty(key, targetType);
        forEachPropertyResolverListener(listener -> listener.afterGetProperty(delegate, key, targetType, value, null));
        return value;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        forEachPropertyResolverListener(
                listener -> listener.beforeGetProperty(delegate, key, targetType, defaultValue));
        T value = delegate.getProperty(key, targetType, defaultValue);
        forEachPropertyResolverListener(
                listener -> listener.afterGetProperty(delegate, key, targetType, value, defaultValue));
        return value;
    }

    @Override
    public String getRequiredProperty(String key) {
        forEachPropertyResolverListener(listener -> listener.beforeGetRequiredProperty(delegate, key, String.class));
        String value = delegate.getRequiredProperty(key);
        forEachPropertyResolverListener(
                listener -> listener.afterGetRequiredProperty(delegate, key, String.class, value));
        return value;
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) {
        forEachPropertyResolverListener(listener -> listener.beforeGetRequiredProperty(delegate, key, targetType));
        T value = delegate.getRequiredProperty(key, targetType);
        forEachPropertyResolverListener(
                listener -> listener.afterGetRequiredProperty(delegate, key, targetType, value));
        return value;
    }

    @Override
    public String resolvePlaceholders(String text) {
        forEachPropertyResolverListener(listener -> listener.beforeResolvePlaceholders(delegate, text));
        String result = delegate.resolvePlaceholders(text);
        forEachPropertyResolverListener(listener -> listener.afterResolvePlaceholders(delegate, text, result));
        return result;
    }

    @Override
    public String resolveRequiredPlaceholders(String text) {
        forEachPropertyResolverListener(listener -> listener.beforeResolveRequiredPlaceholders(delegate, text));
        String result = delegate.resolveRequiredPlaceholders(text);
        forEachPropertyResolverListener(listener -> listener.afterResolveRequiredPlaceholders(delegate, text, result));
        return result;
    }

    @Override
    public ConfigurableConversionService getConversionService() {
        forEachPropertyResolverListener(listener -> listener.beforeGetConversionService(delegate));
        ConfigurableConversionService conversionService = delegate.getConversionService();
        forEachPropertyResolverListener(listener -> listener.afterGetConversionService(delegate, conversionService));
        return conversionService;
    }

    @Override
    public void setConversionService(ConfigurableConversionService conversionService) {
        forEachPropertyResolverListener(listener -> listener.beforeSetConversionService(delegate, conversionService));
        delegate.setConversionService(conversionService);
        forEachPropertyResolverListener(listener -> listener.afterSetConversionService(delegate, conversionService));
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        forEachPropertyResolverListener(listener -> listener.beforeSetPlaceholderPrefix(delegate, placeholderPrefix));
        delegate.setPlaceholderPrefix(placeholderPrefix);
        forEachPropertyResolverListener(listener -> listener.afterSetPlaceholderPrefix(delegate, placeholderPrefix));
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        forEachPropertyResolverListener(listener -> listener.beforeSetPlaceholderSuffix(delegate, placeholderSuffix));
        delegate.setPlaceholderSuffix(placeholderSuffix);
        forEachPropertyResolverListener(listener -> listener.afterSetPlaceholderSuffix(delegate, placeholderSuffix));
    }

    @Override
    public void setValueSeparator(String valueSeparator) {
        forEachPropertyResolverListener(listener -> listener.beforeSetValueSeparator(delegate, valueSeparator));
        delegate.setValueSeparator(valueSeparator);
        forEachPropertyResolverListener(listener -> listener.afterSetValueSeparator(delegate, valueSeparator));
    }

    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        forEachPropertyResolverListener(listener ->
                listener.beforeSetIgnoreUnresolvableNestedPlaceholders(delegate, ignoreUnresolvableNestedPlaceholders));
        delegate.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
        forEachPropertyResolverListener(listener ->
                listener.afterSetIgnoreUnresolvableNestedPlaceholders(delegate, ignoreUnresolvableNestedPlaceholders));
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        forEachPropertyResolverListener(listener -> listener.beforeSetRequiredProperties(delegate, requiredProperties));
        delegate.setRequiredProperties(requiredProperties);
        forEachPropertyResolverListener(listener -> listener.afterSetRequiredProperties(delegate, requiredProperties));
    }

    @Override
    public void validateRequiredProperties() throws MissingRequiredPropertiesException {
        forEachPropertyResolverListener(listener -> listener.beforeValidateRequiredProperties(delegate));
        delegate.validateRequiredProperties();
        forEachPropertyResolverListener(listener -> listener.afterValidateRequiredProperties(delegate));
    }

    public void setEscapeCharacter(Character escapeCharacter) {
        if (SET_ESCAPE_CHARACTER_METHOD_HANDLE != null) {
            throw new UnsupportedOperationException("escapeCharacter is not supported in this simplified bridge");
        }
    }

    public ConfigurableEnvironment getDelegate() {
        return delegate;
    }

    private void forEachEnvironmentListener(java.util.function.Consumer<EnvironmentListener> consumer) {
        for (EnvironmentListener listener : environmentListeners) {
            consumer.accept(listener);
        }
    }

    private void forEachProfileListener(java.util.function.Consumer<ProfileListener> consumer) {
        for (ProfileListener listener : profileListeners) {
            consumer.accept(listener);
        }
    }

    private void forEachPropertyResolverListener(java.util.function.Consumer<PropertyResolverListener> consumer) {
        for (PropertyResolverListener listener : propertyResolverListeners) {
            consumer.accept(listener);
        }
    }
}
