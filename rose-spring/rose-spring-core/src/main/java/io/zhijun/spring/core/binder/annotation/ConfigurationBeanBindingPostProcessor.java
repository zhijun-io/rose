package io.zhijun.spring.core.binder.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.zhijun.spring.core.binder.config.ConfigurationBeanBinder;
import io.zhijun.spring.core.binder.config.ConfigurationBeanCustomizer;
import io.zhijun.spring.core.binder.config.DefaultConfigurationBeanBinder;
import io.zhijun.spring.core.binder.support.ConversionServiceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ClassUtils;

import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * Binds configuration beans registered by {@link EnableConfigurationBeanBinding}.
 */
public class ConfigurationBeanBindingPostProcessor implements BeanPostProcessor, BeanFactoryAware, PriorityOrdered {

    public static final String BEAN_NAME = "configurationBeanBindingPostProcessor";

    static final String CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME = "configurationProperties";

    static final String IGNORE_UNKNOWN_FIELDS_ATTRIBUTE_NAME = "ignoreUnknownFields";

    static final String IGNORE_INVALID_FIELDS_ATTRIBUTE_NAME = "ignoreInvalidFields";

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationBeanBindingPostProcessor.class);

    private ConfigurableListableBeanFactory beanFactory;

    private ConfigurationBeanBinder configurationBeanBinder;

    private List<ConfigurationBeanCustomizer> configurationBeanCustomizers;

    private int order = PriorityOrdered.LOWEST_PRECEDENCE;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        BeanDefinition beanDefinition = getNullableBeanDefinition(beanName);
        if (isConfigurationBean(bean, beanDefinition)) {
            bindConfigurationBean(bean, beanDefinition);
            customize(beanName, bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ConfigurationBeanBinder getConfigurationBeanBinder() {
        if (configurationBeanBinder == null) {
            initConfigurationBeanBinder();
        }
        return configurationBeanBinder;
    }

    public void setConfigurationBeanBinder(ConfigurationBeanBinder configurationBeanBinder) {
        this.configurationBeanBinder = configurationBeanBinder;
    }

    public List<ConfigurationBeanCustomizer> getConfigurationBeanCustomizers() {
        if (configurationBeanCustomizers == null) {
            initConfigurationBeanCustomizers();
        }
        return configurationBeanCustomizers;
    }

    public void setConfigurationBeanCustomizers(Collection<ConfigurationBeanCustomizer> configurationBeanCustomizers) {
        List<ConfigurationBeanCustomizer> customizers = new ArrayList<ConfigurationBeanCustomizer>(configurationBeanCustomizers);
        AnnotationAwareOrderComparator.sort(customizers);
        this.configurationBeanCustomizers = Collections.unmodifiableList(customizers);
    }

    static void initBeanMetadataAttributes(AbstractBeanDefinition beanDefinition,
            Map<String, Object> configurationProperties, boolean ignoreUnknownFields, boolean ignoreInvalidFields) {
        beanDefinition.setAttribute(CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME, configurationProperties);
        beanDefinition.setAttribute(IGNORE_UNKNOWN_FIELDS_ATTRIBUTE_NAME, ignoreUnknownFields);
        beanDefinition.setAttribute(IGNORE_INVALID_FIELDS_ATTRIBUTE_NAME, ignoreInvalidFields);
    }

    private BeanDefinition getNullableBeanDefinition(String beanName) {
        return beanFactory.containsBeanDefinition(beanName) ? beanFactory.getBeanDefinition(beanName) : null;
    }

    private boolean isConfigurationBean(Object bean, BeanDefinition beanDefinition) {
        return beanDefinition != null
                && ConfigurationBeanBindingRegistrar.ENABLE_CONFIGURATION_BINDING_CLASS.equals(beanDefinition.getSource())
                && nullSafeEquals(ClassUtils.getUserClass(bean.getClass()).getName(), beanDefinition.getBeanClassName());
    }

    private void bindConfigurationBean(Object configurationBean, BeanDefinition beanDefinition) {
        Map<String, Object> configurationProperties = getAttribute(beanDefinition, CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME);
        boolean ignoreUnknownFields = getAttribute(beanDefinition, IGNORE_UNKNOWN_FIELDS_ATTRIBUTE_NAME);
        boolean ignoreInvalidFields = getAttribute(beanDefinition, IGNORE_INVALID_FIELDS_ATTRIBUTE_NAME);
        getConfigurationBeanBinder().bind(configurationProperties, ignoreUnknownFields, ignoreInvalidFields,
                configurationBean);
        if (logger.isDebugEnabled()) {
            logger.debug("Bound configuration bean [{}] with properties {}", configurationBean, configurationProperties);
        }
    }

    private void initConfigurationBeanBinder() {
        ConfigurationBeanBinder binder = configurationBeanBinder;
        if (binder == null) {
            try {
                binder = beanFactory.getBean(ConfigurationBeanBinder.class);
            } catch (BeansException ignored) {
                binder = new DefaultConfigurationBeanBinder();
            }
        }
        ConversionService conversionService = new ConversionServiceResolver(beanFactory).resolve();
        binder.setConversionService(conversionService);
        configurationBeanBinder = binder;
    }

    private void initConfigurationBeanCustomizers() {
        Collection<ConfigurationBeanCustomizer> customizers = beansOfTypeIncludingAncestors(beanFactory,
                ConfigurationBeanCustomizer.class).values();
        setConfigurationBeanCustomizers(customizers);
    }

    private void customize(String beanName, Object configurationBean) {
        for (ConfigurationBeanCustomizer customizer : getConfigurationBeanCustomizers()) {
            customizer.customize(beanName, configurationBean);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getAttribute(BeanDefinition beanDefinition, String attributeName) {
        return (T) beanDefinition.getAttribute(attributeName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
