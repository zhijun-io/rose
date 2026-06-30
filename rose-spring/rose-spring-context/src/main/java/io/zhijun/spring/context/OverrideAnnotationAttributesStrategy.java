package io.zhijun.spring.context;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import io.zhijun.core.annotation.Nullable;

/**
 * 注解属性覆盖策略接口。
 * <p>
 * SPI 层只做发现和排序，剩下的都是上层框架（Spring）或应用层的事。
 *
 * @see OverrideAnnotationAttributes
 * @see ConfigurationPropertyOverrideAnnotationAttributesStrategy
 */
public interface OverrideAnnotationAttributesStrategy {

    /**
     * 覆盖注解属性值。
     *
     * @param originalAttributes 原始注解属性
     * @param annotationType     注解类型
     * @param annotationMetadata 被注解类的元数据
     * @return 覆盖后的属性（返回 {@code null} 表示不做覆盖）
     */
    @Nullable
    AnnotationAttributes override(AnnotationAttributes originalAttributes,
                                  Class<? extends Annotation> annotationType,
                                  AnnotationMetadata annotationMetadata);
}
