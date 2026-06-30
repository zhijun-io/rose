package io.zhijun.spring.context;

import io.zhijun.spring.env.PropertySourcesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.jspecify.annotations.Nullable;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 通过配置属性覆盖注解属性的策略实现。
 * <p>
 * 将注解属性值外部化到 {@code application.properties} / {@code application.yml} 中，
 * 使用约定的属性名前缀。
 *
 * <h3>属性名前缀规则</h3>
 * <ol>
 *     <li>优先使用自定义前缀：{@code rose.spring.prefix.<全限定注解类名>}
 *         （如 {@code rose.spring.prefix.org.springframework.context.annotation.PropertySource}）</li>
 *     <li>回退到默认前缀：{@code rose.spring.@<简单类名>.}
 *         （如 {@code rose.spring.@PropertySource.}）</li>
 * </ol>
 *
 * 前缀确定后，注解属性名附加在前缀后形成完整配置键。
 *
 * @see OverrideAnnotationAttributesStrategy
 * @see PropertySourcesUtils
 */
public class ConfigurationPropertyOverrideAnnotationAttributesStrategy
        implements OverrideAnnotationAttributesStrategy, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationPropertyOverrideAnnotationAttributesStrategy.class);

    private static final String ROSE_SPRING_PROPERTY_PREFIX = "rose.spring.";

    private static final String PREFIX_PROPERTY_NAME_PREFIX = ROSE_SPRING_PROPERTY_PREFIX + "prefix.";

    @Nullable
    private ConfigurableEnvironment environment;

    @Override
    public AnnotationAttributes override(AnnotationAttributes originalAttributes,
                                         Class<? extends Annotation> annotationType,
                                         AnnotationMetadata annotationMetadata) {
        Map<String, Object> configProps = getConfigurationProperties(annotationType);
        if (configProps.isEmpty()) {
            logger.debug("未找到注解 [{}] 的配置属性，跳过覆盖", annotationType);
            return originalAttributes;
        }
        ConversionService conversionService = environment.getConversionService();
        AnnotationAttributes result = new AnnotationAttributes(originalAttributes.size());
        for (Entry<String, Object> entry : originalAttributes.entrySet()) {
            String attrName = entry.getKey();
            Object originalValue = entry.getValue();
            Object configValue = configProps.get(attrName);
            if (configValue == null) {
                result.put(attrName, originalValue);
                continue;
            }
            // 获取属性类型
            Class<?> attrType = originalValue != null ? originalValue.getClass() : String.class;
            try {
                Method attrMethod = annotationType.getMethod(attrName);
                attrType = attrMethod.getReturnType();
            } catch (NoSuchMethodException e) {
                // 即使找不到方法，继续处理
            }
            if (conversionService.canConvert(configValue.getClass(), attrType)) {
                Object converted = conversionService.convert(configValue, attrType);
                result.put(attrName, converted);
                logger.info("配置属性 [{}] 已覆盖注解属性 [{}]：{}", attrName, annotationType.getSimpleName(), converted);
            } else {
                result.put(attrName, originalValue);
                logger.warn("配置属性 [{}] 的值无法转换为类型 [{}]，保留原始值", attrName, attrType);
            }
        }
        return result;
    }

    /**
     * 获取注解关联的配置属性。
     */
    protected Map<String, Object> getConfigurationProperties(Class<? extends Annotation> annotationType) {
        String prefix = getPropertyNamePrefix(annotationType);
        return PropertySourcesUtils.getSubProperties(environment, prefix);
    }

    /**
     * 获取属性名前缀。
     */
    protected String getPropertyNamePrefix(Class<? extends Annotation> annotationType) {
        String prefixPropertyName = getPrefixPropertyName(annotationType);
        String prefix = environment.getProperty(prefixPropertyName, String.class, getDefaultPropertyNamePrefix(annotationType));
        return PropertySourcesUtils.normalizePrefix(prefix);
    }

    /**
     * 获取自定义前缀的配置属性名：{@code rose.spring.prefix.<全限定名>}
     */
    public static String getPrefixPropertyName(Class<? extends Annotation> annotationType) {
        return PREFIX_PROPERTY_NAME_PREFIX + annotationType.getName();
    }

    /**
     * 获取默认属性名前缀：{@code rose.spring.@<简单类名>.}
     */
    public static String getDefaultPropertyNamePrefix(Class<? extends Annotation> annotationType) {
        return ROSE_SPRING_PROPERTY_PREFIX + "@" + annotationType.getSimpleName() + ".";
    }

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            this.environment = (ConfigurableEnvironment) environment;
        } else {
            this.environment = null;
            logger.warn("Environment [{}] is not ConfigurableEnvironment, property override disabled", environment);
        }
    }
}
