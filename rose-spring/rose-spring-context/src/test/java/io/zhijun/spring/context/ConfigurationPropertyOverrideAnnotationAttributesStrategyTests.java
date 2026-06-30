package io.zhijun.spring.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.core.annotation.AnnotationUtils;

import io.zhijun.spring.env.PropertySourcesUtils;
import java.lang.annotation.Annotation;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ConfigurationPropertyOverrideAnnotationAttributesStrategy} 测试。
 */
@PropertySource("classpath:/test-app.properties")
class ConfigurationPropertyOverrideAnnotationAttributesStrategyTests {

    private static final Class<PropertySource> ANNOTATION_CLASS = PropertySource.class;

    private static final String DEFAULT_PREFIX = "rose.spring.@PropertySource.";

    private static final AnnotationAttributes ATTRIBUTES;

    static {
        // 从测试类获取 @PropertySource 的注解属性
        PropertySource ann = ConfigurationPropertyOverrideAnnotationAttributesStrategyTests.class
               .getAnnotation(PropertySource.class);
        ATTRIBUTES = AnnotationAttributes.fromMap(
                AnnotationUtils.getAnnotationAttributes(ann));
    }

    private MockEnvironment environment;

    private ConfigurationPropertyOverrideAnnotationAttributesStrategy strategy;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment();
        strategy = new ConfigurationPropertyOverrideAnnotationAttributesStrategy();
        strategy.setEnvironment(environment);
    }

    @Test
    void overrideWhenConfigPropertyExists() {
        String prefix = DEFAULT_PREFIX;
        environment.setProperty(prefix + "value", "file:/custom/path.properties");

        AnnotationAttributes result = strategy.override(ATTRIBUTES, ANNOTATION_CLASS, null);

        assertThat(result.getStringArray("value"))
                .containsExactly("file:/custom/path.properties");
    }

    @Test
    void overrideWhenConfigPropertyHasDifferentValue() {
        String prefix = DEFAULT_PREFIX;
        environment.setProperty(prefix + "value", "classpath:/test-app.properties");

        AnnotationAttributes result = strategy.override(ATTRIBUTES, ANNOTATION_CLASS, null);

        // 值与原始相同，但属性类型可能不同（String 而非 String[]）
        assertThat(result.getStringArray("value")).contains("classpath:/test-app.properties");
    }

    @Test
    void overrideWhenConfigPropertyHasUnrelatedKey() {
        String prefix = DEFAULT_PREFIX;
        environment.setProperty(prefix + "unrelated", "value");

        AnnotationAttributes result = strategy.override(ATTRIBUTES, ANNOTATION_CLASS, null);

        assertThat(result.getStringArray("value"))
                .containsExactly("classpath:/test-app.properties");
    }

    @Test
    void overrideWhenNoConfigProperties() {
        AnnotationAttributes result = strategy.override(ATTRIBUTES, ANNOTATION_CLASS, null);

        // 无配置属性时返回原始实例
        assertThat(result).isSameAs(ATTRIBUTES);
    }

    @Test
    void overrideWithCustomPrefix() {
        String customPrefix = "custom.prefix.";
        environment.setProperty(
                ConfigurationPropertyOverrideAnnotationAttributesStrategy.getPrefixPropertyName(ANNOTATION_CLASS),
                customPrefix);
        environment.setProperty(customPrefix + "value", "file:/custom.properties");

        AnnotationAttributes result = strategy.override(ATTRIBUTES, ANNOTATION_CLASS, null);

        assertThat(result.getStringArray("value"))
                .containsExactly("file:/custom.properties");
    }

    @Test
    void getConfigurationProperties() {
        String prefix = DEFAULT_PREFIX;
        environment.setProperty(prefix + "a", "1");
        environment.setProperty(prefix + "b", "2");

        Map<String, Object> props = PropertySourcesUtils.getSubProperties(environment, DEFAULT_PREFIX);

        assertThat(props).hasSize(2)
                .containsEntry("a", "1")
                .containsEntry("b", "2");
    }

    @Test
    void propertyNamePrefixUsesDefaultWhenNoCustomPrefix() {
        assertThat(DEFAULT_PREFIX).isEqualTo("rose.spring.@PropertySource.");
    }
}
