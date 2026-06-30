package io.zhijun.core.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * SPI 实现类优先级。值越小优先级越高。
  *
  * <p>替代 {@code javax.annotation.Priority}——后者不属于标准 JDK API。
  *
  * <p><b>职责边界</b>：SPI 层只利用本注解对实现类进行排序，不做控制反转
  * 或条件过滤。默认值 {@link Integer#MAX_VALUE} 表示最低优先级。
  */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Priority {

    /**
     * 优先级值。值越小优先级越高。
     */
    int value() default Integer.MAX_VALUE;
}
