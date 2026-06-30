package io.zhijun.core.spi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * 标记一个类型为 SPI 扩展点。
  *
  * <p><b>职责边界</b>：SPI 层只做发现和排序。框架或应用层负责实例化策略
  *（例如 Spring 扫描 {@code @Spi} 接口的所有实现并注册为 Bean）。
  */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Spi {}
