package io.zhijun.spring.config.context.annotation;

import io.zhijun.spring.config.env.support.DefaultResourceComparator;
import io.zhijun.spring.core.annotation.AnnotationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PropertySourceExtensionAttributesTests.class)
@ResourcePropertySource(
        name = "test-property-source",
        value = {"classpath*:/META-INF/test/*.properties"},
        ignoreResourceNotFound = true,
        encoding = "UTF-8"
)
class PropertySourceExtensionAttributesTests {

    @Autowired
    private Environment environment;

    private PropertySourceExtensionAttributes<ResourcePropertySource> attributes;

    @BeforeEach
    void setUp() {
        AnnotationAttributes annotationAttributes = (AnnotationAttributes) org.springframework.core.annotation.AnnotationUtils
                .getAnnotationAttributes(getClass().getAnnotation(ResourcePropertySource.class), false);
        this.attributes = new PropertySourceExtensionAttributes<ResourcePropertySource>(
                AnnotationUtils.ofAnnotationAttributes(annotationAttributes), ResourcePropertySource.class, environment);
    }

    @Test
    void shouldValidateAnnotationType() {
        assertThat(PropertySourceExtensionAttributes.validateAnnotationType(ResourcePropertySource.class))
                .isSameAs(ResourcePropertySource.class);
    }

    @Test
    void shouldRejectNonExtensionAnnotation() {
        assertThatThrownBy(() -> PropertySourceExtensionAttributes.validateAnnotationType(Override.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldExposeConfiguredAttributes() {
        assertThat(attributes.getName()).isEqualTo("test-property-source");
        assertThat(attributes.isAutoRefreshed()).isFalse();
        assertThat(attributes.isFirstPropertySource()).isFalse();
        assertThat(attributes.getBeforePropertySourceName()).isEmpty();
        assertThat(attributes.getAfterPropertySourceName()).isEmpty();
        assertThat(attributes.getValue()).containsExactly("classpath*:/META-INF/test/*.properties");
        assertThat(attributes.getResourceComparatorClass()).isEqualTo(DefaultResourceComparator.class);
        assertThat(attributes.isIgnoreResourceNotFound()).isTrue();
        assertThat(attributes.getEncoding()).isEqualTo("UTF-8");
        assertThat(attributes.getPropertySourceFactoryClass()).isEqualTo(DefaultPropertySourceFactory.class);
        assertThat(attributes.getAnnotationType()).isEqualTo(ResourcePropertySource.class);
    }
}
