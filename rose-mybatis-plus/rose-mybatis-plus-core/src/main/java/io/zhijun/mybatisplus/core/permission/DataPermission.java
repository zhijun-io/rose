package io.zhijun.mybatisplus.core.permission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares data permission metadata on mapper interfaces.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataPermission {

    /**
     * SQL table alias.
     */
    String alias() default "";

    /**
     * Column used for permission filtering.
     */
    String column() default "";

    /**
     * Mapper methods excluded from permission injection.
     */
    String[] ignoreMethods() default {};
}
