package io.zhijun.mybatisplus.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import io.zhijun.mybatisplus.core.extension.MybatisPlusInterceptorCustomizer;
import io.zhijun.mybatisplus.spring.extension.MyBatisPlusExtensionConfiguration;

/**
 * Enables annotation-driven MyBatis-Plus extension, allowing {@link
 * MybatisPlusInterceptorCustomizer} beans and
 * {@code spring.factories}-discovered customizers to be applied to the
 * {@link com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor}.
 *
 * <p>In Spring Framework (non-Boot) environments, annotate a {@code @Configuration}
 * class with this annotation to activate the customizer registration. In Spring Boot
 * environments this is handled automatically by auto-configuration.
 *
 * <pre>{@code
 *   @Configuration
 *   @EnableMyBatisPlusExtension
 *   public class MyBatisConfig {
 *       @Bean
 *       public MybatisPlusInterceptorCustomizer softDeleteCustomizer() {
 *           return interceptor -> interceptor.addInnerInterceptor(new SoftDeleteInnerInterceptor());
 *       }
 *   }
 * }</pre>
 *
 * @see MyBatisPlusExtensionConfiguration
 * @since 0.0.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MyBatisPlusExtensionConfiguration.class)
public @interface EnableMyBatisPlusExtension {}
