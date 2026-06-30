package io.zhijun.multitenancy.spring.web.filter;

import org.springframework.http.server.PathContainer;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Matches HTTP request paths for which a multitenancy identifier is required.
 */

public final class TenantContextRequiredPathMatcher {

    private final List<PathPattern> includePathPatterns;

    private final List<PathPattern> excludePathPatterns;

    public TenantContextRequiredPathMatcher(Set<String> includePathPatterns, Set<String> excludePathPatterns) {
        Assert.notNull(includePathPatterns, "includePathPatterns cannot be null");
        Assert.notNull(excludePathPatterns, "excludePathPatterns cannot be null");
        this.includePathPatterns = toPathPatterns(includePathPatterns);
        this.excludePathPatterns = toPathPatterns(excludePathPatterns);
    }

    public boolean requires(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "httpServletRequest cannot be null");
        if (CollectionUtils.isEmpty(includePathPatterns)) {
            return false;
        }
        String requestUri = normalizeUri(httpServletRequest.getRequestURI());
        PathContainer pathContainer = PathContainer.parsePath(requestUri);
        boolean included = includePathPatterns.stream().anyMatch(pattern -> pattern.matches(pathContainer));
        if (!included) {
            return false;
        }
        return excludePathPatterns.stream().noneMatch(pattern -> pattern.matches(pathContainer));
    }

    private static String normalizeUri(String requestUri) {
        return requestUri == null ? "" : requestUri.replace("//", "/");
    }

    private static List<PathPattern> toPathPatterns(Set<String> patterns) {
        if (CollectionUtils.isEmpty(patterns)) {
            return Collections.emptyList();
        }
        PathPatternParser parser = PathPatternParser.defaultInstance;
        return patterns.stream()
                .map(pattern -> parser.parse(parser.initFullPathPattern(pattern)))
                .collect(Collectors.toList());
    }
}
