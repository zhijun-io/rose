package io.zhijun.spring.web.rule;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;

import io.zhijun.spring.web.util.WebRequestUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 按 Accept 生产类型匹配的规则（逻辑或）。
 */
public class WebRequestProducesRule extends AbstractWebRequestRule<ProduceMediaTypeExpression> {

    private static final ContentNegotiationManager DEFAULT_CONTENT_NEGOTIATION_MANAGER =
            new ContentNegotiationManager();

    private static final String MEDIA_TYPES_ATTRIBUTE = WebRequestProducesRule.class.getName() + ".MEDIA_TYPES";

    private final List<ProduceMediaTypeExpression> expressions;

    private final ContentNegotiationManager contentNegotiationManager;

    public WebRequestProducesRule(String... produces) {
        this(produces, null);
    }

    public WebRequestProducesRule(String @Nullable [] produces, @Nullable String... headers) {
        this(produces, headers, null);
    }

    public WebRequestProducesRule(String @Nullable [] produces, @Nullable String[] headers,
                                  @Nullable ContentNegotiationManager manager) {
        this.expressions = ProduceMediaTypeExpression.parseExpressions(produces, headers);
        if (this.expressions.size() > 1) {
            Collections.sort(this.expressions);
        }
        this.contentNegotiationManager = manager != null ? manager : DEFAULT_CONTENT_NEGOTIATION_MANAGER;
    }

    @Override
    protected Collection<ProduceMediaTypeExpression> getContent() {
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

        List<MediaType> acceptedMediaTypes;
        try {
            acceptedMediaTypes = getAcceptedMediaTypes(request);
        } catch (HttpMediaTypeException ex) {
            return false;
        }

        List<ProduceMediaTypeExpression> result = getMatchingExpressions(acceptedMediaTypes);
        if (result.isEmpty()) return false;
        return !isPresent(MediaType.ALL, acceptedMediaTypes);
    }

    private List<ProduceMediaTypeExpression> getMatchingExpressions(List<MediaType> acceptedMediaTypes) {
        List<ProduceMediaTypeExpression> result = new ArrayList<>(this.expressions.size());
        for (ProduceMediaTypeExpression expression : this.expressions) {
            if (expression.match(acceptedMediaTypes)) {
                result.add(expression);
            }
        }
        return result;
    }

    private List<MediaType> getAcceptedMediaTypes(NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {
        @SuppressWarnings("unchecked")
        List<MediaType> result = (List<MediaType>) request.getAttribute(MEDIA_TYPES_ATTRIBUTE,
                NativeWebRequest.SCOPE_REQUEST);
        if (result == null) {
            result = this.contentNegotiationManager.resolveMediaTypes(request);
            request.setAttribute(MEDIA_TYPES_ATTRIBUTE, result, NativeWebRequest.SCOPE_REQUEST);
        }
        return result;
    }

    private static boolean isPresent(MediaType mediaType, List<MediaType> mediaTypes) {
        for (MediaType mt : mediaTypes) {
            if (mediaType.equals(mt)) return true;
        }
        return false;
    }
}
