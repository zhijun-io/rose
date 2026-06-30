 package io.zhijun.spring.boot.web.autoconfigure;

 import io.zhijun.spring.boot.properties.annotation.EnableConfigurationPropertiesExtension;

 import org.springframework.boot.autoconfigure.AutoConfigureBefore;
 import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
 import org.springframework.boot.context.properties.EnableConfigurationProperties;

 /**
 * Auto-configuration that enables {@link EnableConfigurationPropertiesExtension} for
 * advanced {@link EnableConfigurationProperties @EnableConfigurationProperties} binding features.
 * <p>
 * Activates before Spring Boot's own
 * {@code org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration}.
 * <p>
 * Inspired by {@code io.microsphere.spring.boot.context.autoconfigure.ConfigurationPropertiesAutoConfiguration}.
 */
 @ConditionalOnClass(name = {
         "io.zhijun.spring.context.annotation.BeanCapableImportCandidate"
 })
 @AutoConfigureBefore(name = {
         "org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration"
 })
 @EnableConfigurationPropertiesExtension
 public class ConfigurationPropertiesAutoConfiguration {
 }
