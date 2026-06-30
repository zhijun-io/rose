package io.zhijun.spring.boot.properties;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.hash;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} Bean 信息
 */
public class ConfigurationPropertiesBeanInfo {

    private final Class<?> beanClass;

    private final ConfigurationProperties annotation;

    private final String prefix;

    private final PropertyDescriptor[] propertyDescriptors;

    public ConfigurationPropertiesBeanInfo(Class<?> beanClass) {
        this(beanClass, beanClass.getAnnotation(ConfigurationProperties.class));
    }

    public ConfigurationPropertiesBeanInfo(Class<?> beanClass, ConfigurationProperties annotation) {
        this(beanClass, annotation, hasText(annotation.prefix()) ? annotation.prefix() : annotation.value());
    }

    public ConfigurationPropertiesBeanInfo(Class<?> beanClass, ConfigurationProperties annotation, String prefix) {
        Objects.requireNonNull(beanClass, "'beanClass' must not be null");
        Objects.requireNonNull(annotation, "'annotation' must not be null");
        Objects.requireNonNull(prefix, "'prefix' must not be null");
        this.beanClass = beanClass;
        this.annotation = annotation;
        this.prefix = prefix;
        this.propertyDescriptors = BeanUtils.getPropertyDescriptors(beanClass);
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public ConfigurationProperties getAnnotation() {
        return annotation;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<PropertyDescriptor> getPropertyDescriptors() {
        return Arrays.asList(propertyDescriptors);
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return BeanUtils.getPropertyDescriptor(beanClass, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationPropertiesBeanInfo)) return false;
        ConfigurationPropertiesBeanInfo that = (ConfigurationPropertiesBeanInfo) o;
        return Objects.equals(beanClass, that.beanClass) && Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return hash(beanClass, prefix);
    }

    @Override
    public String toString() {
        return "ConfigurationPropertiesBeanInfo{" +
                "beanClass=" + beanClass +
                ", annotation=" + annotation +
                ", prefix='" + prefix + '\'' +
                ", propertyDescriptors=" + Arrays.toString(propertyDescriptors) +
                '}';
    }
}
