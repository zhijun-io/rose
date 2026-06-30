package io.zhijun.spring.web.rule;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;

import io.zhijun.spring.web.util.WebRequestUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 按 HTTP 方法匹配的规则（逻辑或），CORS 预检请求时不匹配。
 */
public class WebRequestMethodsRule extends AbstractWebRequestRule<String> {

    private final Set<String> methods;

    public WebRequestMethodsRule(RequestMethod... requestMethods) {
        this.methods = requestMethods == null || requestMethods.length == 0
                ? Collections.emptySet()
                : Stream.of(requestMethods).map(RequestMethod::name).collect(Collectors.toSet());
    }

    public WebRequestMethodsRule(String method, String... others) {
        Set<String> set = new LinkedHashSet<>();
        set.add(method);
        if (others != null) Collections.addAll(set, others);
        this.methods = set;
    }

    @Override
    protected Collection<String> getContent() {
        return methods;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        if (WebRequestUtils.isPreFlightRequest(request)) return false;
        String method = WebRequestUtils.getMethod(request);
        return matches(method);
    }

    public boolean matches(String method) {
        if (isEmpty()) return !HttpMethod.OPTIONS.name().equals(method);
        return matchRequestMethod(method);
    }

    private boolean matchRequestMethod(String method) {
        for (String requestMethod : this.methods) {
            if (requestMethod.equalsIgnoreCase(method)) return true;
        }
        return false;
    }
}
