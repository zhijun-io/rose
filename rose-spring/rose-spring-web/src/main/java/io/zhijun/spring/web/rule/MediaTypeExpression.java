package io.zhijun.spring.web.rule;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 媒体类型表达式契约，用于 {@link RequestMapping#consumes()} 和 {@link RequestMapping#produces()}。
 */
public interface MediaTypeExpression {

    MediaType getMediaType();

    boolean isNegated();
}
