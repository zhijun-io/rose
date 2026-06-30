package io.zhijun.spring.web.rule;

import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;
import java.util.List;

/**
 * 按请求参数匹配的规则（逻辑与），基于参数表达式。
 */
public class WebRequestParamsRule extends AbstractWebRequestRule<WebRequestParamExpression> {

    private final List<WebRequestParamExpression> expressions;

    public WebRequestParamsRule(String... params) {
        this.expressions = WebRequestParamExpression.parseExpressions(params);
    }

    @Override
    protected Collection<WebRequestParamExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        for (WebRequestParamExpression expression : expressions) {
            if (!expression.match(request)) return false;
        }
        return true;
    }
}
