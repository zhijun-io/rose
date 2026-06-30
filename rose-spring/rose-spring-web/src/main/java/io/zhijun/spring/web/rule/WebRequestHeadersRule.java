package io.zhijun.spring.web.rule;

import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;
import java.util.List;

/**
 * 按请求头匹配的规则（逻辑与），基于头表达式。
 */
public class WebRequestHeadersRule extends AbstractWebRequestRule<WebRequestHeaderExpression> {

    private final List<WebRequestHeaderExpression> expressions;

    public WebRequestHeadersRule(String... headers) {
        this.expressions = WebRequestHeaderExpression.parseExpressions(headers);
    }

    @Override
    protected Collection<WebRequestHeaderExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        for (WebRequestHeaderExpression expression : expressions) {
            if (!expression.match(request)) return false;
        }
        return true;
    }
}
