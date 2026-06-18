package io.zhijun.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotation that indicates the API is introduced in the first time.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Incubating
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target({
        TYPE,
        FIELD,
        METHOD,
        PARAMETER,
        CONSTRUCTOR,
        LOCAL_VARIABLE,
        ANNOTATION_TYPE,
        PACKAGE,
        TYPE_PARAMETER,
        TYPE_USE
})
@Documented
public @interface Since {

    /**
     * @return The module name, e.g. <code>microsphere-core</code>
     */
    String module() default "";

    /**
     * @return The version value of the API, e.g. <code>1.0.0</code>
     */
    String value();

}
