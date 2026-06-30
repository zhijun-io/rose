package io.zhijun.spring.web.rule;

import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 复合 {@link WebRequestRule}（逻辑与），包含的子规则全部匹配才匹配。
 */
public class CompositeWebRequestRule implements WebRequestRule {

    private final List<WebRequestRule> webRequestRules;

    public CompositeWebRequestRule(WebRequestRule... requestRules) {
        this.webRequestRules = requestRules != null && requestRules.length > 0
                ? Arrays.asList(requestRules)
                : Collections.emptyList();
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        for (WebRequestRule webRequestRule : webRequestRules) {
            if (!webRequestRule.matches(request)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeWebRequestRule that = (CompositeWebRequestRule) o;
        return Objects.equals(webRequestRules, that.webRequestRules);
    }

    @Override
    public int hashCode() {
        return webRequestRules.hashCode();
    }

    @Override
    public String toString() {
        return webRequestRules.toString();
    }
}
