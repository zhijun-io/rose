package io.zhijun.core.spi.annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
  * 标记一个 SPI 实现类。
  *
  * <p><b>职责边界</b>：本注解仅提供实现类的元数据（别名、优先级、描述、标签），
  * 供 {@code SpiImplProcessor} 在编译期生成服务描述文件和 IDE 工具使用。
  * 实例化策略由上层框架决定。
  */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpiImpl {
    /**
     * 实现别名。默认取类名且首字母小写。
     */
    String value() default "";
    /**
     * 显式优先级。值越小优先级越高。
     */
    int priority() default Integer.MAX_VALUE;

    /**
     * 实现的可读描述。
     */
    String description() default "";

    /**
     * 功能分类标签。
     */
    String[] tags() default {};
}
