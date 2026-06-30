package io.zhijun.spring.boot.properties.bind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;
import static org.springframework.util.ClassUtils.isAssignableValue;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} Bean 上下文
 */
class ConfigurationPropertiesBeanContext {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationPropertiesBeanContext.class);

    private final String beanName;

    private final ResolvableType beanType;

    private final String prefix;

    private final ConfigurableApplicationContext context;

    private final Map<ConfigurationPropertyName, ConfigurationPropertiesBeanProperty> beanProperties = new HashMap<>();

    private Object initializedBean;

    private BeanWrapper beanWrapper;

    ConfigurationPropertiesBeanContext(String beanName, ResolvableType beanType, String prefix,
                                       ConfigurableApplicationContext context) {
        this.beanName = beanName;
        this.beanType = beanType;
        this.prefix = prefix;
        this.context = context;
    }

    String getBeanName() {
        return beanName;
    }

    ResolvableType getBeanType() {
        return beanType;
    }

    String getPrefix() {
        return prefix;
    }

    Class<?> getBeanClass() {
        return getBeanType().getRawClass();
    }

    void setBean(Object bean) {
        if (!beanType.isInstance(bean)) {
            logger.warn("The bean[{}] is not an instance of {}", bean.getClass(), beanType);
            return;
        }
        this.initializedBean = bean;
        initBeanProperties();
    }

    private void initBeanProperties() {
        Object bean = getBean();
        this.beanWrapper = new BeanWrapperImpl(bean);
        Class<?> beanClass = getBeanClass();
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(beanClass);
        ConfigurationPropertyName prefixName = of(this.prefix);
        for (PropertyDescriptor descriptor : descriptors) {
            initBeanProperty(descriptor, prefixName, null);
        }
    }

    private void initBeanProperty(PropertyDescriptor descriptor, ConfigurationPropertyName prefixName, String nestedPath) {
        Method readMethod = descriptor.getReadMethod();
        if (readMethod == null || Object.class.equals(readMethod.getDeclaringClass())) {
            return;
        }
        String propertyName = descriptor.getName();
        String propertyPath = nestedPath != null ? nestedPath + "." + propertyName : propertyName;
        ConfigurationPropertyName configurationPropertyName = prefixName.append(toDashedForm(propertyName));
        ConfigurationPropertiesBeanProperty property = beanProperties.computeIfAbsent(configurationPropertyName, name -> {
            ConfigurationPropertiesBeanProperty newProperty = new ConfigurationPropertiesBeanProperty();
            newProperty.setDeclaringClassType(beanType);
            newProperty.setName(propertyPath);
            newProperty.setGetter(descriptor.getReadMethod());
            newProperty.setSetter(descriptor.getWriteMethod());
            newProperty.setValue(getPropertyValue(propertyPath));
            return newProperty;
        });
        // Handle nested properties
        Class<?> propertyType = descriptor.getPropertyType();
        if (propertyType != null && isCandidateClass(propertyType)) {
            ConfigurationPropertyName nestedPrefix = configurationPropertyName;
            PropertyDescriptor[] nestedDescriptors = BeanUtils.getPropertyDescriptors(propertyType);
            for (PropertyDescriptor nestedDesc : nestedDescriptors) {
                initBeanProperty(nestedDesc, nestedPrefix, propertyPath);
            }
        }
    }

    void bindPropertyValues() {
        beanProperties.values().forEach(this::bindPropertyValue);
    }

    boolean bindPropertyValue(ConfigurationPropertiesBeanProperty beanProperty) {
        String propertyPath = beanProperty.getName();
        Object newValue = getPropertyValue(propertyPath);
        return setPropertyValue(beanProperty, beanProperty.getValue(), newValue, false);
    }

    Object getBean() {
        if (initializedBean == null) {
            initializedBean = context.getBean(beanName, getBeanClass());
            initBeanProperties();
        }
        return initializedBean;
    }

    Object getPropertyValue(String propertyPath) {
        if (beanWrapper != null && beanWrapper.isReadableProperty(propertyPath)) {
            return beanWrapper.getPropertyValue(propertyPath);
        }
        return null;
    }

    boolean setPropertyValue(ConfigurationPropertiesBeanProperty beanProperty, Object oldValue, Object newValue, boolean resolved) {
        if (oldValue == newValue || (oldValue != null && oldValue.equals(newValue))) {
            return false;
        }
        beanProperty.setValue(newValue);
        if (logger.isTraceEnabled()) {
            logger.trace("Set property [path: '{}'] from '{}' to '{}'", beanProperty.getName(), oldValue, newValue);
        }
        return true;
    }

    void setProperty(ConfigurationProperty property, Object newValue) {
        ConfigurationPropertyName name = property.getName();
        if (name.isLastElementIndexed()) {
            name = name.getParent();
        }
        ConfigurationPropertiesBeanProperty beanProperty = beanProperties.get(name);
        if (beanProperty == null) {
            name = name.getParent();
            beanProperty = beanProperties.get(name);
        }
        if (beanProperty == null) {
            return;
        }
        ResolvableType propertyType = beanProperty.getType();
        Class<?> propertyClass = propertyType.resolve();
        if (propertyClass != null && isAssignableValue(propertyClass, newValue)) {
            Object oldValue = beanProperty.getValue();
            if (setPropertyValue(beanProperty, oldValue, newValue, true)) {
                publishEvent(property, beanProperty, oldValue, newValue);
            }
        }
    }

    ConfigurationPropertiesBeanProperty getProperty(ConfigurationPropertyName propertyName) {
        return beanProperties.get(propertyName);
    }

    void publishEvent(ConfigurationProperty property, ConfigurationPropertiesBeanProperty beanProperty,
                      Object oldValue, Object newValue) {
        context.publishEvent(new ConfigurationPropertiesBeanPropertyChangedEvent(
                getBean(), beanProperty.getName(), beanProperty.getType(), oldValue, newValue, property));
    }

    static String toDashedForm(String name) {
        StringBuilder result = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(ch));
            } else if (ch == '_') {
                result.append('-');
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    static boolean isCandidateClass(Class<?> beanClass) {
        if (isPrimitiveOrWrapper(beanClass) || beanClass.isEnum() || beanClass.isArray()
                || beanClass.getName().startsWith("java.")) {
            return false;
        }
        return true;
    }

    static Map<String, ConfigurationPropertiesBeanContext> buildConfigurationPropertiesBeanContexts(
            ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        Map<String, ConfigurationPropertiesBeanContext> beanContexts = new HashMap<>();
        for (String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
            ResolvableType beanType = beanDefinition.getResolvableType();
            Class<?> beanClass = beanType.resolve();
            if (beanClass != null) {
                ConfigurationProperties annotation = AnnotationUtils.findAnnotation(beanClass, ConfigurationProperties.class);
                if (annotation != null) {
                    String prefix = hasText(annotation.prefix()) ? annotation.prefix() : annotation.value();
                    ConfigurationPropertiesBeanContext beanContext = new ConfigurationPropertiesBeanContext(
                            beanName, beanType, prefix, context);
                    beanContexts.put(prefix, beanContext);
                }
            }
        }
        return beanContexts;
    }
}
