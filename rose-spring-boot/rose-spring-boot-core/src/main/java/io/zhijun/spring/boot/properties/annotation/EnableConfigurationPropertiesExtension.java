 package io.zhijun.spring.boot.properties.annotation;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import org.springframework.boot.context.properties.EnableConfigurationProperties;
 import org.springframework.context.annotation.Import;
 
 import static java.lang.annotation.ElementType.TYPE;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 /**
 * Extension annotation for {@link EnableConfigurationProperties @EnableConfigurationProperties}
 * that enables advanced binding features:
 * <ul>
 *     <li>Automatic advising of {@link io.zhijun.spring.boot.properties.bind.BindListener}
 *         implementations during {@link org.springframework.boot.context.properties.bind.Binder binding}</li>
 *     <li>Event publishing for {@link org.springframework.boot.context.properties.ConfigurationProperties}
 *         bean property changes</li>
 * </ul>
 * <p>
 * Inspired by {@code io.microsphere.spring.boot.context.properties.annotation.EnableConfigurationPropertiesExtension}.
 */
 @Target(TYPE)
 @Retention(RUNTIME)
 @Documented
 @Import(EnableConfigurationPropertiesExtensionRegistrar.class)
 public @interface EnableConfigurationPropertiesExtension {
 
     /**
      * Whether to publish {@link io.zhijun.spring.boot.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent}
      * when bound properties change.
      */
     boolean publishEvents() default true;
 }
