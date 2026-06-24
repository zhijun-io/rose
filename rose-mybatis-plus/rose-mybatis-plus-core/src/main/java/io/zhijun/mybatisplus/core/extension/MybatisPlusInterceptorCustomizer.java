package io.zhijun.mybatisplus.core.extension;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

/**
 * SPI for customizing the {@link MybatisPlusInterceptor} registered by MyBatis-Plus.
 * <p>
 * Implementations can add {@link com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor
 * InnerInterceptor}s, reorder existing ones, or apply any other configuration.
 * <p>
 * Customizers are discovered from two sources:
 * <ul>
 *     <li>Spring Bean (when running under Spring, via {@code @Bean} or auto-configuration)</li>
 *     <li>{@code META-INF/spring.factories} (for third-party jars that need no Spring wiring)</li>
 * </ul>
 * <p>
 * Example:
 * <pre>{@code
 *   public class SoftDeleteInterceptorCustomizer implements MybatisPlusInterceptorCustomizer {
 *       @Override
 *       public void customize(MybatisPlusInterceptor interceptor) {
 *           interceptor.addInnerInterceptor(new SoftDeleteInnerInterceptor());
 *       }
 *   }
 * }</pre>
 *
 * @see MybatisPlusInterceptor
 * @see io.zhijun.mybatisplus.spring.extension.MybatisPlusInterceptorCustomizerBeanPostProcessor
 * @since 0.0.1
 */
public interface MybatisPlusInterceptorCustomizer {

    /**
     * Customize the given {@link MybatisPlusInterceptor}.
     *
     * @param interceptor the interceptor to customize; never {@code null}
     */
    void customize(MybatisPlusInterceptor interceptor);
}
