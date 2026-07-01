package io.zhijun.spring.test.jdbc.embedded;

import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.zhijun.spring.test.jdbc.embedded.EmbeddedDatabaseType.SQLITE;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 启用嵌入式数据库
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(EmbeddedDataBaseBeanDefinitionRegistrar.class)
@Repeatable(EnableEmbeddedDatabases.class)
public @interface EnableEmbeddedDatabase {

    EmbeddedDatabaseType type() default SQLITE;

    int port() default -1;

    String dataSource();

    boolean primary() default false;

    String[] properties() default {};
}
