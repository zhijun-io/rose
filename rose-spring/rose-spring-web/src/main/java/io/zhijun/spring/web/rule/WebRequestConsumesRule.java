 package io.zhijun.spring.web.rule;
 
 import org.springframework.http.MediaType;
 import org.springframework.web.context.request.NativeWebRequest;
 import org.jspecify.annotations.Nullable;
 
 import io.zhijun.spring.web.util.WebRequestUtils;
 import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 按 Content-Type 消费类型匹配的规则（逻辑或）。
 */
public class WebRequestConsumesRule extends AbstractWebRequestRule<ConsumeMediaTypeExpression> {

    private final List<ConsumeMediaTypeExpression> expressions;

    public WebRequestConsumesRule(String... consumes) {
        this(consumes, null);
    }

    public WebRequestConsumesRule(String @Nullable [] consumes, @Nullable String... headers) {
        this.expressions = ConsumeMediaTypeExpression.parseExpressions(consumes, headers);
    }

    @Override
    protected Collection<ConsumeMediaTypeExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        if (WebRequestUtils.isPreFlightRequest(request)) return false;
        if (isEmpty()) return false;

        MediaType contentType = WebRequestUtils.parseContentType(request);
        if (contentType == null) return false;

        List<ConsumeMediaTypeExpression> result = getMatchingExpressions(contentType);
        return !result.isEmpty();
    }

    List<ConsumeMediaTypeExpression> getMatchingExpressions(MediaType contentType) {
        List<ConsumeMediaTypeExpression> result = new ArrayList<>(this.expressions.size());
        for (ConsumeMediaTypeExpression expression : this.expressions) {
            if (expression.match(contentType)) {
                result.add(expression);
            }
        }
        return result;
    }
}
