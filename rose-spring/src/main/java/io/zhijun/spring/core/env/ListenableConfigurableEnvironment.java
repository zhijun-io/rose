package io.zhijun.spring.core.env;

import io.zhijun.spring.core.SpringFactoriesLoaderUtils;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.MissingRequiredPropertiesException;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 可监听的 {@link ConfigurableEnvironment} 实现，在 Environment/PropertyResolver/Profile 各操作前后触发监听器。
 *
 * @see EnvironmentListener
 * @see ProfileListener
 * @see PropertyResolverListener
 */
public class ListenableConfigurableEnvironment implements ConfigurableEnvironment {

    private final ConfigurableEnvironment delegate;

    private final List<EnvironmentListener> environmentListeners;

    private final List<ProfileListener> profileListeners;

    private final List<PropertyResolverListener> propertyResolverListeners;

    public ListenableConfigurableEnvironment(ClassLoader classLoader, ConfigurableEnvironment delegate) {
        this.delegate = delegate;
        this.environmentListeners = loadListeners(classLoader, EnvironmentListener.class);
        this.profileListeners = loadListeners(classLoader, ProfileListener.class);
        this.propertyResolverListeners = loadListeners(classLoader, PropertyResolverListener.class);
    }

    private static <T> List<T> loadListeners(ClassLoader classLoader, Class<T> listenerType) {
        return SpringFactoriesLoaderUtils.loadFactories(listenerType, classLoader);
    }

    @Override
    public void setActiveProfiles(String... profiles) {
        forEach(profileListeners, l -> l.beforeSetActiveProfiles(delegate, profiles));
        delegate.setActiveProfiles(profiles);
        forEach(profileListeners, l -> l.afterSetActiveProfiles(delegate, profiles));
    }

    @Override
    public void addActiveProfile(String profile) {
        forEach(profileListeners, l -> l.beforeAddActiveProfile(delegate, profile));
        delegate.addActiveProfile(profile);
        forEach(profileListeners, l -> l.afterAddActiveProfile(delegate, profile));
    }

    @Override
    public void setDefaultProfiles(String... profiles) {
        forEach(profileListeners, l -> l.beforeSetDefaultProfiles(delegate, profiles));
        delegate.setDefaultProfiles(profiles);
        forEach(profileListeners, l -> l.afterSetDefaultProfiles(delegate, profiles));
    }

    @Override
    public MutablePropertySources getPropertySources() {
        forEach(environmentListeners, l -> l.beforeGetPropertySources(delegate));
        MutablePropertySources sources = delegate.getPropertySources();
        forEach(environmentListeners, l -> l.afterGetPropertySources(delegate, sources));
        return sources;
    }

    @Override
    public Map<String, Object> getSystemProperties() {
        forEach(environmentListeners, l -> l.beforeGetSystemProperties(delegate));
        Map<String, Object> properties = delegate.getSystemProperties();
        forEach(environmentListeners, l -> l.afterGetSystemProperties(delegate, properties));
        return properties;
    }

    @Override
    public Map<String, Object> getSystemEnvironment() {
        forEach(environmentListeners, l -> l.beforeGetSystemEnvironment(delegate));
        Map<String, Object> env = delegate.getSystemEnvironment();
        forEach(environmentListeners, l -> l.afterGetSystemEnvironment(delegate, env));
        return env;
    }

    @Override
    public void merge(ConfigurableEnvironment parent) {
        forEach(environmentListeners, l -> l.beforeMerge(delegate, parent));
        delegate.merge(parent);
        forEach(environmentListeners, l -> l.afterMerge(delegate, parent));
    }

    @Override
    public String[] getActiveProfiles() {
        forEach(profileListeners, l -> l.beforeGetActiveProfiles(delegate));
        String[] profiles = delegate.getActiveProfiles();
        forEach(profileListeners, l -> l.afterGetActiveProfiles(delegate, profiles));
        return profiles;
    }

    @Override
    public String[] getDefaultProfiles() {
        forEach(profileListeners, l -> l.beforeGetDefaultProfiles(delegate));
        String[] profiles = delegate.getDefaultProfiles();
        forEach(profileListeners, l -> l.afterGetDefaultProfiles(delegate, profiles));
        return profiles;
    }

    @Override
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
        return getProperty(key, String.class, null);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return getProperty(key, String.class, defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        forEach(propertyResolverListeners, l -> l.beforeGetProperty(delegate, key, targetType, defaultValue));
        T value = delegate.getProperty(key, targetType, defaultValue);
        forEach(propertyResolverListeners, l -> l.afterGetProperty(delegate, key, targetType, value, defaultValue));
        return value;
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return getRequiredProperty(key, String.class);
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        forEach(propertyResolverListeners, l -> l.beforeGetRequiredProperty(delegate, key, targetType));
        T value = delegate.getRequiredProperty(key, targetType);
        forEach(propertyResolverListeners, l -> l.afterGetRequiredProperty(delegate, key, targetType, value));
        return value;
    }

    @Override
    public String resolvePlaceholders(String text) {
        forEach(propertyResolverListeners, l -> l.beforeResolvePlaceholders(delegate, text));
        String result = delegate.resolvePlaceholders(text);
        forEach(propertyResolverListeners, l -> l.afterResolvePlaceholders(delegate, text, result));
        return result;
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        forEach(propertyResolverListeners, l -> l.beforeResolveRequiredPlaceholders(delegate, text));
        String result = delegate.resolveRequiredPlaceholders(text);
        forEach(propertyResolverListeners, l -> l.afterResolveRequiredPlaceholders(delegate, text, result));
        return result;
    }

    @Override
    public ConfigurableConversionService getConversionService() {
        forEach(propertyResolverListeners, l -> l.beforeGetConversionService(delegate));
        ConfigurableConversionService svc = delegate.getConversionService();
        forEach(propertyResolverListeners, l -> l.afterGetConversionService(delegate, svc));
        return svc;
    }

    @Override
    public void setConversionService(ConfigurableConversionService conversionService) {
        forEach(propertyResolverListeners, l -> l.beforeSetConversionService(delegate, conversionService));
        delegate.setConversionService(conversionService);
        forEach(propertyResolverListeners, l -> l.afterSetConversionService(delegate, conversionService));
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        forEach(propertyResolverListeners, l -> l.beforeSetPlaceholderPrefix(delegate, placeholderPrefix));
        delegate.setPlaceholderPrefix(placeholderPrefix);
        forEach(propertyResolverListeners, l -> l.afterSetPlaceholderPrefix(delegate, placeholderPrefix));
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        forEach(propertyResolverListeners, l -> l.beforeSetPlaceholderSuffix(delegate, placeholderSuffix));
        delegate.setPlaceholderSuffix(placeholderSuffix);
        forEach(propertyResolverListeners, l -> l.afterSetPlaceholderSuffix(delegate, placeholderSuffix));
    }

    @Override
    public void setValueSeparator(String valueSeparator) {
        forEach(propertyResolverListeners, l -> l.beforeSetValueSeparator(delegate, valueSeparator));
        delegate.setValueSeparator(valueSeparator);
        forEach(propertyResolverListeners, l -> l.afterSetValueSeparator(delegate, valueSeparator));
    }

    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        forEach(propertyResolverListeners, l -> l.beforeSetIgnoreUnresolvableNestedPlaceholders(delegate, ignoreUnresolvableNestedPlaceholders));
        delegate.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
        forEach(propertyResolverListeners, l -> l.afterSetIgnoreUnresolvableNestedPlaceholders(delegate, ignoreUnresolvableNestedPlaceholders));
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        forEach(propertyResolverListeners, l -> l.beforeSetRequiredProperties(delegate, requiredProperties));
        delegate.setRequiredProperties(requiredProperties);
        forEach(propertyResolverListeners, l -> l.afterSetRequiredProperties(delegate, requiredProperties));
    }

    @Override
    public void validateRequiredProperties() throws MissingRequiredPropertiesException {
        forEach(propertyResolverListeners, l -> l.beforeValidateRequiredProperties(delegate));
        delegate.validateRequiredProperties();
        forEach(propertyResolverListeners, l -> l.afterValidateRequiredProperties(delegate));
    }

    public ConfigurableEnvironment getDelegate() {
        return this.delegate;
    }

    private <T> void forEach(List<T> listeners, Consumer<T> consumer) {
        if (listeners == null || listeners.isEmpty()) return;
        for (T listener : listeners) {
            consumer.accept(listener);
        }
    }
}
