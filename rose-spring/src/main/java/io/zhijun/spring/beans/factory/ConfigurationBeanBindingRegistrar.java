package io.zhijun.spring.beans.factory;

import io.zhijun.spring.context.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.zhijun.spring.beans.factory.BeanRegistrar.registerBeanDefinition;
import static io.zhijun.spring.beans.factory.BeanRegistrar.registerInfrastructureBean;
import static io.zhijun.spring.beans.factory.ConfigurationBeanBindingPostProcessor.BEAN_NAME;
import static io.zhijun.spring.beans.factory.ConfigurationBeanBindingPostProcessor.initBeanMetadataAttributes;
import static io.zhijun.spring.beans.factory.EnableConfigurationBeanBinding.*;
import static io.zhijun.spring.core.AnnotationUtils.getAttribute;
import static io.zhijun.spring.core.AnnotationUtils.getRequiredAttribute;
import static io.zhijun.spring.core.SpringFactoriesLoaderUtils.loadFactories;
import static io.zhijun.spring.core.env.PropertySourcesUtils.getSubProperties;
import static io.zhijun.spring.core.env.PropertySourcesUtils.normalizePrefix;
import static java.lang.Boolean.valueOf;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.generateBeanName;
import static org.springframework.util.StringUtils.hasText;

/**
 * A registrar for registering {@link EnableConfigurationBeanBinding @EnableConfigurationBeanBinding}-annotated bean definitions.
 * <p>
 * This class processes the {@link EnableConfigurationBeanBinding} annotation, binding its attributes to corresponding Spring beans.
 * It supports configuration options such as prefix resolution, bean naming strategies, and handling of unknown or invalid fields.
 * </p>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Basic Configuration</h4>
 * <pre>{@code
 * @Configuration
 * @EnableConfigurationBeanBinding(prefix = "my.config", type = MyConfig.class)
 * public class MyConfig {}
 * }</pre>
 *
 * <h4>Multiple Bean Registration</h4>
 * <pre>{@code
 * @Configuration
 * @EnableConfigurationBeanBinding(prefix = "multi.config", type = MultiConfig.class, multiple = true)
 * public class MultiConfig {}
 * }</pre>
 *
 * <h4>Custom Ignore Behavior</h4>
 * <pre>{@code
 * @Configuration
 * @EnableConfigurationBeanBinding(
 *     prefix = "strict.config",
 *     type = StrictConfig.class,
 *     ignoreUnknownFields = false,
 *     ignoreInvalidFields = false
 * )
 * public class StrictConfig {}
 * }</pre>
 *
 * @see EnableConfigurationBeanBinding
 * @see ConfigurationBeanBindingPostProcessor
 * @since 1.0.0
 */
public class ConfigurationBeanBindingRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableConfigurationBeanBinding> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableConfigurationBeanBinding> annotationAttributes) {
        registerConfigurationBeanDefinitions(annotationAttributes, registry);
    }

    public void registerConfigurationBeanDefinitions(Map<String, Object> attributes, BeanDefinitionRegistry registry) {

        String prefix = getRequiredAttribute(attributes, "prefix");

        prefix = this.environment.resolvePlaceholders(prefix);

        Class<?> configClass = getRequiredAttribute(attributes, "type");

        boolean multiple = getAttribute(attributes, "multiple", valueOf(DEFAULT_MULTIPLE));

        boolean ignoreUnknownFields = getAttribute(attributes, "ignoreUnknownFields", valueOf(DEFAULT_IGNORE_UNKNOWN_FIELDS));

        boolean ignoreInvalidFields = getAttribute(attributes, "ignoreInvalidFields", valueOf(DEFAULT_IGNORE_INVALID_FIELDS));

        registerConfigurationBeans(prefix, configClass, multiple, ignoreUnknownFields, ignoreInvalidFields, registry);
    }

    private void registerConfigurationBeans(String prefix, Class<?> configClass, boolean multiple,
                                            boolean ignoreUnknownFields, boolean ignoreInvalidFields,
                                            BeanDefinitionRegistry registry) {

        Map<String, Object> configurationProperties = getSubProperties(this.environment.getPropertySources(), this.environment, prefix);

        Set<String> beanNames = multiple ? resolveMultipleBeanNames(configurationProperties) :
                new java.util.LinkedHashSet<>(java.util.Collections.singleton(resolveSingleBeanName(configurationProperties, configClass, registry)));

        for (String beanName : beanNames) {
            registerConfigurationBean(beanName, configClass, multiple, ignoreUnknownFields, ignoreInvalidFields,
                    configurationProperties, registry);

            registerConfigurationBeanAlias(beanName, configClass, prefix, registry);
        }

        registerConfigurationBindingBeanPostProcessor(registry);
    }

    private void registerConfigurationBeanAlias(String beanName, Class<?> configClass, String prefix, BeanDefinitionRegistry registry) {
        List<ConfigurationBeanAliasGenerator> configurationBeanAliasGenerators = loadFactories(beanFactory, ConfigurationBeanAliasGenerator.class);
        configurationBeanAliasGenerators.forEach(aliasGenerator -> {
            String alias = aliasGenerator.generateAlias(prefix, beanName, configClass);
            registry.registerAlias(beanName, alias);
        });

    }

    private void registerConfigurationBean(String beanName, Class<?> configClass, boolean multiple,
                                           boolean ignoreUnknownFields, boolean ignoreInvalidFields,
                                           Map<String, Object> configurationProperties,
                                           BeanDefinitionRegistry registry) {

        BeanDefinitionBuilder builder = rootBeanDefinition(configClass);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

        setSource(beanDefinition);

        Map<String, Object> subProperties = resolveSubProperties(multiple, beanName, configurationProperties);

        initBeanMetadataAttributes(beanDefinition, subProperties, ignoreUnknownFields, ignoreInvalidFields);

        registry.registerBeanDefinition(beanName, beanDefinition);

        registerBeanDefinition(registry, beanName, beanDefinition);
    }

    private Map<String, Object> resolveSubProperties(boolean multiple, String beanName,
                                                     Map<String, Object> configurationProperties) {
        if (!multiple) {
            return configurationProperties;
        }

        MutablePropertySources propertySources = new MutablePropertySources();

        propertySources.addLast(new MapPropertySource("_", configurationProperties));

        return getSubProperties(propertySources, environment, normalizePrefix(beanName));
    }

    private void setSource(AbstractBeanDefinition beanDefinition) {
        beanDefinition.setSource(getAnnotationType());
    }

    private void registerConfigurationBindingBeanPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, BEAN_NAME, ConfigurationBeanBindingPostProcessor.class);
    }

    private Set<String> resolveMultipleBeanNames(Map<String, Object> properties) {

        Set<String> beanNames = new java.util.LinkedHashSet<>();

        for (String propertyName : properties.keySet()) {

            int index = propertyName.indexOf('.');

            if (index > 0) {

                String beanName = propertyName.substring(0, index);

                beanNames.add(beanName);
            }

        }

        return beanNames;

    }

    private String resolveSingleBeanName(Map<String, Object> properties, Class<?> configClass,
                                         BeanDefinitionRegistry registry) {

        String beanName = (String) properties.get("id");

        if (!hasText(beanName)) {
            BeanDefinitionBuilder builder = rootBeanDefinition(configClass);
            beanName = generateBeanName(builder.getRawBeanDefinition(), registry);
        }

        return beanName;

    }
}
