package io.zhijun.core.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * 标记一个 SPI 实现类，供 {@code SpiImplProcessor} 在编译期自动生成
  * {@code META-INF/services/} 服务描述文件。
  *
  * <p><b>职责边界</b>：本注解只标记实现类。排序由 {@link Priority @Priority} 负责，
  * 实例化策略由上层框架（Spring / Spring Boot）决定。
  */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpiImpl {}
