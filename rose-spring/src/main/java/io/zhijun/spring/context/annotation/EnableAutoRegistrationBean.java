package io.zhijun.spring.context.annotation;

import io.zhijun.spring.context.config.AutoRegistrationBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.zhijun.spring.constants.PropertyConstants.AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;
import static io.zhijun.spring.constants.PropertyConstants.BEANS_PROPERTY_NAME_PREFIX;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME; /**
 * Enable auto-registration Spring beans that implement the interface {@link AutoRegistrationBean} are loaded by
 * {@link SpringFactoriesLoader Spring Factories SPI}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AutoRegistrationBean
 * @see AutoRegistrationBeanRegistrar
 * @see SpringFactoriesLoader
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
@Import(AutoRegistrationBeanRegistrar.class)
public @interface EnableAutoRegistrationBean {

    /**
     * Environment property that can be used to override when auto-registration of Spring Beans is enabled.
     */
    String BEANS_AUTO_REGISTERED_PROEPRTY_NAME = BEANS_PROPERTY_NAME_PREFIX + AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;
}
