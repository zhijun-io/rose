package io.zhijun.core.spi.annotation;
import io.zhijun.core.spi.ClassPresentCondition;
import java.lang.annotation.*;
/**
 * SPI加载条件：指定类存在时才加载实现
 * <p>不依赖Spring，全环境可用
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(OnClassPresent.List.class)
@ConditionAnnotation(ClassPresentCondition.class)
public @interface OnClassPresent {
    /**
     * 要检查的类全限定名
     */
    String[] value();
    /**
     * 是否需要所有类都存在才匹配，默认true
     * <p>为false时只要有一个类存在就匹配
     */
    boolean allMatch() default true;
    /**
     * 条件匹配时是否加载SPI，默认true
     * <p>为false时类存在则不加载，不存在才加载
     */
    boolean matches() default true;
    /**
     * 分组注解，支持多个@OnClassPresent条件
     */
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OnClassPresent[] value();
    }
}
