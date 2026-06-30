package io.zhijun.spring.context;

import io.zhijun.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 元注解，标记在自定义注解上，表示注解属性可被外部覆盖。
 * <p>
 * 通过指定自定义的 {@link OverrideAnnotationAttributesStrategy} 实现，
 * 可以在注册 Bean 之前修改注解属性值。
 *
 * @see OverrideAnnotationAttributesStrategy
 * @see ConfigurationPropertyOverrideAnnotationAttributesStrategy
 * @see BeanCapableImportCandidate#getOverriddenAnnotationAttributes
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface OverrideAnnotationAttributes {

    /**
     * 注解属性覆盖策略类。必须有无参构造器。
     *
     * @return 策略实现类，默认使用 {@link ConfigurationPropertyOverrideAnnotationAttributesStrategy}
     */
    Class<? extends OverrideAnnotationAttributesStrategy> strategy() default ConfigurationPropertyOverrideAnnotationAttributesStrategy.class;

}
