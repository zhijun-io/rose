package io.zhijun.spring.web.rule;

import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 名值表达式契约，用于 {@link RequestMapping#params()} 和 {@link RequestMapping#headers()} 的 "name!=value" 风格匹配。
 */
public interface NameValueExpression<T> {

    String getName();

    @Nullable
    T getValue();

    boolean isNegated();
}
