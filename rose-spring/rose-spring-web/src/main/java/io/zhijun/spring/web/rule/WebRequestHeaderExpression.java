package io.zhijun.spring.web.rule;

import org.springframework.web.context.request.NativeWebRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 请求头名值表达式。
 */
public class WebRequestHeaderExpression extends AbstractNameValueExpression<String> {

    public WebRequestHeaderExpression(String expression) {
        super(expression);
    }

    @Override
    protected String parseValue(String valueExpression) {
        return valueExpression;
    }

    @Override
    protected boolean matchName(NativeWebRequest request) {
        return request.getHeader(this.name) != null;
    }

    @Override
    protected boolean matchValue(NativeWebRequest request) {
        String[] values = request.getHeaderValues(this.name);
        if (values == null) return false;
        for (String v : values) {
            if (this.value != null && this.value.equals(v)) return true;
        }
        return false;
    }

    public static List<WebRequestHeaderExpression> parseExpressions(String... headers) {
        if (headers == null || headers.length == 0) {
            return Collections.emptyList();
        }
        List<WebRequestHeaderExpression> expressions = new ArrayList<>(headers.length);
        for (String header : headers) {
            if ("Accept".equalsIgnoreCase(header) || "Content-Type".equalsIgnoreCase(header)) {
                continue;
            }
            expressions.add(new WebRequestHeaderExpression(header));
        }
        return expressions;
    }
}
