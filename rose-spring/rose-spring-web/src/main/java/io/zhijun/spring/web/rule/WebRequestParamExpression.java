package io.zhijun.spring.web.rule;

import org.springframework.web.context.request.NativeWebRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 请求参数名值表达式。
 */
public class WebRequestParamExpression extends AbstractNameValueExpression<String> {

    public WebRequestParamExpression(String expression) {
        super(expression, true);
    }

    @Override
    protected String parseValue(String valueExpression) {
        return valueExpression;
    }

    @Override
    protected boolean matchName(NativeWebRequest request) {
        return request.getParameter(this.name) != null;
    }

    @Override
    protected boolean matchValue(NativeWebRequest request) {
        String[] parameterValues = request.getParameterValues(this.name);
        if (parameterValues == null) return false;
        for (String pv : parameterValues) {
            if (this.value != null && this.value.equals(pv)) return true;
        }
        return false;
    }

    public static List<WebRequestParamExpression> parseExpressions(String... params) {
        if (params == null || params.length == 0) {
            return Collections.emptyList();
        }
        List<WebRequestParamExpression> expressions = new ArrayList<>(params.length);
        for (String param : params) {
            expressions.add(new WebRequestParamExpression(param));
        }
        return expressions;
    }
}
