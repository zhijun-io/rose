package io.zhijun.spring.core.spi.annotation;
import io.zhijun.core.spi.annotation.ConditionAnnotation;
import io.zhijun.spring.core.spi.OnProfileCondition;
import java.lang.annotation.*;
/**
 * SPI加载条件：指定Profile激活时才加载实现
 * <p>Spring环境专用
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(OnProfile.List.class)
@ConditionAnnotation(OnProfileCondition.class)
public @interface OnProfile {
    /**
     * 要匹配的Profile名称
     */
    String[] value();
    /**
     * 是否需要所有Profile都激活才匹配，默认true
     * <p>为false时只要有一个Profile激活就匹配
     */
    boolean allMatch() default true;
    /**
     * 条件匹配时是否加载SPI，默认true
     * <p>为false时Profile激活则不加载，不激活才加载
     */
    boolean matches() default true;
    /**
     * 分组注解，支持多个@OnProfile条件
     */
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OnProfile[] value();
    }
}
