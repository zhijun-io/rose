package io.zhijun.multitenancy.spring.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.PathContainer;
import org.springframework.util.Assert;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Matches HTTP requests paths for which a multitenancy context is not attached.
 */

public class TenantContextIgnorePathMatcher {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextIgnorePathMatcher.class);

    private final List<PathPattern> ignorePathPatterns;

    public TenantContextIgnorePathMatcher(Set<String> ignorePathPatterns) {
        Assert.notNull(ignorePathPatterns, "ignorePathPatterns cannot be null");
        this.ignorePathPatterns = ignorePathPatterns.stream().map(this::parse).collect(Collectors.toList());
    }

    public boolean matches(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "httpServletRequest cannot be null");
        String requestUri = httpServletRequest.getRequestURI();
        PathContainer pathContainer = PathContainer.parsePath(requestUri);
        boolean matchesIgnorePaths =
                ignorePathPatterns.stream().anyMatch(pathPattern -> pathPattern.matches(pathContainer));
        if (matchesIgnorePaths) {
            logger.debug(
                    "Request '{}' matches one of the paths to ignore when attaching a multitenancy context",
                    sanitizeForLog(requestUri));
        }
        return matchesIgnorePaths;
    }

    private static String sanitizeForLog(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\r\n\t]", "_");
    }

    private PathPattern parse(String pattern) {
        PathPatternParser parser = PathPatternParser.defaultInstance;
        pattern = parser.initFullPathPattern(pattern);
        return parser.parse(pattern);
    }
}
