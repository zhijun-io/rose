package io.zhijun.spring.core.binder.annotation;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import io.zhijun.spring.core.binder.support.ConfigurationBeanAliasGenerator;
import io.zhijun.spring.core.binder.support.ConfigurationBeanBindingSupport;
import io.zhijun.spring.core.env.PropertySourcesUtils;
import io.zhijun.spring.core.io.support.SpringFactoriesLoaderUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import static io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding.DEFAULT_IGNORE_INVALID_FIELDS;
import static io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding.DEFAULT_IGNORE_UNKNOWN_FIELDS;
import static io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding.DEFAULT_MULTIPLE;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.generateBeanName;

/**
 * {@link ImportBeanDefinitionRegistrar} for {@link EnableConfigurationBeanBinding}.
 * <p>
 * Reads {@code prefix.*} from the {@code Environment}, registers one or more bean definitions
 * (see {@link EnableConfigurationBeanBinding#multiple()}), stores binding metadata on each definition,
 * registers bean aliases via {@link io.zhijun.spring.core.binder.support.ConfigurationBeanAliasGenerator},
 * and ensures {@link ConfigurationBeanBindingPostProcessor} is present.
 */
public class ConfigurationBeanBindingRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    public static final Class<?> ENABLE_CONFIGURATION_BINDING_CLASS = EnableConfigurationBeanBinding.class;

    private static final String ENABLE_CONFIGURATION_BINDING_CLASS_NAME = ENABLE_CONFIGURATION_BINDING_CLASS.getName();

    private ConfigurableEnvironment environment;

    private BeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ENABLE_CONFIGURATION_BINDING_CLASS_NAME);
        if (attributes != null) {
            registerConfigurationBeanDefinitions(attributes, registry);
        }
    }

    /**
     * Registers beans for one {@link EnableConfigurationBeanBinding} declaration.
     * Exposed for {@link ConfigurationBeanBindingsRegistrar} delegation.
     */
    public void registerConfigurationBeanDefinitions(Map<String, Object> attributes, BeanDefinitionRegistry registry) {
        String prefix = (String) attributes.get("prefix");
        if (!StringUtils.hasText(prefix)) {
            throw new IllegalStateException("The 'prefix' attribute is required");
        }
        prefix = environment.resolvePlaceholders(prefix);

        Class<?> configClass = (Class<?>) attributes.get("type");
        if (configClass == null) {
            throw new IllegalStateException("The 'type' attribute is required");
        }

        boolean multiple = getBooleanAttribute(attributes, "multiple", DEFAULT_MULTIPLE);
        boolean ignoreUnknownFields = getBooleanAttribute(attributes, "ignoreUnknownFields", DEFAULT_IGNORE_UNKNOWN_FIELDS);
        boolean ignoreInvalidFields = getBooleanAttribute(attributes, "ignoreInvalidFields", DEFAULT_IGNORE_INVALID_FIELDS);

        registerConfigurationBeans(prefix, configClass, multiple, ignoreUnknownFields, ignoreInvalidFields, registry);
    }

    private void registerConfigurationBeans(String prefix, Class<?> configClass, boolean multiple,
            boolean ignoreUnknownFields, boolean ignoreInvalidFields, BeanDefinitionRegistry registry) {
        Map<String, Object> configurationProperties = PropertySourcesUtils.getSubProperties(environment, prefix);
        Set<String> beanNames = multiple ? resolveMultipleBeanNames(configurationProperties)
                : singletonSet(resolveSingleBeanName(configurationProperties, configClass, registry));

        for (String beanName : beanNames) {
            registerConfigurationBean(beanName, configClass, prefix, multiple, ignoreUnknownFields, ignoreInvalidFields,
                    configurationProperties, registry);
            registerConfigurationBeanAlias(beanName, configClass, prefix, registry);
        }

        registerConfigurationBindingBeanPostProcessor(registry);
    }

    private void registerConfigurationBean(String beanName, Class<?> configClass, String prefix, boolean multiple,
            boolean ignoreUnknownFields, boolean ignoreInvalidFields, Map<String, Object> configurationProperties,
            BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(configClass);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.setSource(ConfigurationBeanBindingSupport.CONFIGURATION_BEAN_SOURCE);

        Map<String, Object> subProperties = ConfigurationBeanBindingSupport.resolveSubProperties(multiple, beanName,
                configurationProperties, environment);
        ConfigurationBeanBindingPostProcessor.initBeanMetadataAttributes(beanDefinition, subProperties,
                ignoreUnknownFields, ignoreInvalidFields, prefix, multiple);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private void registerConfigurationBeanAlias(String beanName, Class<?> configClass, String prefix,
            BeanDefinitionRegistry registry) {
        for (ConfigurationBeanAliasGenerator aliasGenerator : SpringFactoriesLoaderUtils.loadFactories(beanFactory,
                ConfigurationBeanAliasGenerator.class)) {
            registry.registerAlias(beanName, aliasGenerator.generateAlias(prefix, beanName, configClass));
        }
    }

    private void registerConfigurationBindingBeanPostProcessor(BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(ConfigurationBeanBindingPostProcessor.BEAN_NAME)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                    .rootBeanDefinition(ConfigurationBeanBindingPostProcessor.class);
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(ConfigurationBeanBindingPostProcessor.BEAN_NAME, builder.getBeanDefinition());
        }
    }

    private Set<String> resolveMultipleBeanNames(Map<String, Object> properties) {
        Set<String> beanNames = new LinkedHashSet<String>();
        for (String propertyName : properties.keySet()) {
            int index = propertyName.indexOf('.');
            if (index > 0) {
                beanNames.add(propertyName.substring(0, index));
            }
        }
        return beanNames;
    }

    private String resolveSingleBeanName(Map<String, Object> properties, Class<?> configClass,
            BeanDefinitionRegistry registry) {
        Object id = properties.get("id");
        if (id instanceof String && StringUtils.hasText((String) id)) {
            return (String) id;
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(configClass);
        return generateBeanName(builder.getRawBeanDefinition(), registry);
    }

    private static Set<String> singletonSet(String value) {
        Set<String> beanNames = new LinkedHashSet<String>(1);
        beanNames.add(value);
        return beanNames;
    }

    private static boolean getBooleanAttribute(Map<String, Object> attributes, String name, boolean defaultValue) {
        Object value = attributes.get(name);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    void setEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
