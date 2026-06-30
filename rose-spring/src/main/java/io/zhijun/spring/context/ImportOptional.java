package io.zhijun.spring.context;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 按类名选择性导入。与 {@link Import @Import} 不同，目标类不存在于 classpath 时不报错，静默跳过。
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Import(ImportOptionalSelector.class)
public @interface ImportOptional {

    /**
     * 需要导入的类全限定名。不存在的类会被静默跳过。
     */
    String[] value();
}
