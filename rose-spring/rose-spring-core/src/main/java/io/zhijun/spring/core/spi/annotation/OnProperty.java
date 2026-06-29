package io.zhijun.spring.core.spi.annotation;
import io.zhijun.core.spi.condition.annotation.ConditionAnnotation;
import io.zhijun.spring.core.spi.OnPropertyCondition;
import java.lang.annotation.*;
/**
 * SPI加载条件：指定配置项匹配时才加载实现
 * <p>Spring环境专用，支持${}占位符、配置项匹配
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(OnProperty.List.class)
@ConditionAnnotation(OnPropertyCondition.class)
public @interface OnProperty {
    /**
     * 配置项名称，支持${}占位符
     */
    String value();
    /**
     * 配置项期望的值，多个值时只要有一个匹配就可以
     * <p>不指定则只要配置项存在就匹配
     */
    String[] havingValue() default {};
    /**
     * 配置项不存在时是否匹配，默认false
     */
    boolean matchIfMissing() default false;
    /**
     * 条件匹配时是否加载SPI，默认true
     * <p>为false时配置匹配则不加载，不匹配才加载
     */
    boolean matches() default true;
    /**
     * 分组注解，支持多个@OnProperty条件
     */
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OnProperty[] value();
    }
}
