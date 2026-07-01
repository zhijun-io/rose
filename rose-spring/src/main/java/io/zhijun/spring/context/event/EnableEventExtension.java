package io.zhijun.spring.context.event;

import io.zhijun.spring.beans.BeanSource;
import io.zhijun.spring.context.annotation.OverrideAnnotationAttributes;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.zhijun.spring.beans.BeanSource.*;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 启用 Spring 事件扩展。
 *
 * <pre>{@code
 * @EnableEventExtension(intercepted = true, executorForListener = "myExecutor")
 * public class MyConfig { }
 * }</pre>
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@OverrideAnnotationAttributes
@Import(EventExtensionRegistrar.class)
public @interface EnableEventExtension {

    String NO_EXECUTOR = "N/E";

    boolean intercepted() default true;

    String executorForListener() default NO_EXECUTOR;

    BeanSource[] sources() default {BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER};
}
