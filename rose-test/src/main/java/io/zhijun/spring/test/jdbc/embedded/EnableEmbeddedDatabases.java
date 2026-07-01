package io.zhijun.spring.test.jdbc.embedded;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link EnableEmbeddedDatabase} 的容器注解
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Inherited
@Import(EmbeddedDataBaseBeanDefinitionsRegistrar.class)
public @interface EnableEmbeddedDatabases {

    EnableEmbeddedDatabase[] value();
}
