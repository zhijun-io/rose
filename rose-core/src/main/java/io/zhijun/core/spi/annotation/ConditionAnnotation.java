package io.zhijun.core.spi.annotation;
import io.zhijun.core.spi.Condition;
import java.lang.annotation.*;
/**
 * 条件注解元标记，标注某个注解是SPI条件注解
 * <p>所有自定义条件注解都需要标注这个元注解，并指定对应的Condition实现类
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionAnnotation {
    /**
     * 对应的Condition实现类
     */
    Class<? extends Condition> value();
}
