package io.zhijun.spring.web.rule;

import org.jspecify.annotations.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.NativeWebRequest;

import io.zhijun.spring.web.util.WebRequestUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 按 URL 路径模式匹配的规则（逻辑或），基于 AntPathMatcher 做字符串匹配。
 */
public class WebRequestPattensRule extends AbstractWebRequestRule<String> {

    static final Set<String> EMPTY_PATH_PATTERN = Collections.singleton("");

    static final String WILDCARD_EXTENSION = ".*";

    private final Set<String> patterns;

    private final PathMatcher pathMatcher;

    private final boolean useSuffixPatternMatch;

    private final boolean useTrailingSlashMatch;

    private final List<String> fileExtensions = new ArrayList<>();

    public WebRequestPattensRule(String... patterns) {
        this(patterns, true, null);
    }

    public WebRequestPattensRule(String[] patterns, boolean useTrailingSlashMatch,
                                 @Nullable PathMatcher pathMatcher) {
        this(patterns, pathMatcher, false, useTrailingSlashMatch);
    }

    public WebRequestPattensRule(String[] patterns, @Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch,
                                 boolean useTrailingSlashMatch) {
        this(patterns, pathMatcher, useSuffixPatternMatch, useTrailingSlashMatch, null);
    }

    public WebRequestPattensRule(String[] patterns, @Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch,
                                 boolean useTrailingSlashMatch, @Nullable List<String> fileExtensions) {
        this.patterns = initPatterns(patterns);
        this.pathMatcher = pathMatcher != null ? pathMatcher : new AntPathMatcher();
        this.useSuffixPatternMatch = useSuffixPatternMatch;
        this.useTrailingSlashMatch = useTrailingSlashMatch;

        if (fileExtensions != null) {
            for (String fileExtension : fileExtensions) {
                if (fileExtension.charAt(0) != '.') {
                    fileExtension = '.' + fileExtension;
                }
                this.fileExtensions.add(fileExtension);
            }
        }
    }

    @Override
    protected Collection<String> getContent() {
        return patterns;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        if (WebRequestUtils.isPreFlightRequest(request)) return false;
        String lookupPath = WebRequestUtils.getResolvedLookupPath(request);
        return matches(lookupPath);
    }

    public boolean matches(String lookupPath) {
        List<String> matches = getMatchingPatterns(lookupPath);
        return !matches.isEmpty();
    }

    public List<String> getMatchingPatterns(String lookupPath) {
        List<String> matches = null;
        for (String pattern : this.patterns) {
            String match = getMatchingPattern(pattern, lookupPath);
            if (match != null) {
                if (matches == null) matches = new ArrayList<>();
                matches.add(match);
            }
        }
        if (matches == null) return Collections.emptyList();
        if (matches.size() > 1) {
            matches.sort(this.pathMatcher.getPatternComparator(lookupPath));
        }
        return matches;
    }

    @Nullable
    protected String getMatchingPattern(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath)) return pattern;

        if (this.useSuffixPatternMatch) {
            if (!this.fileExtensions.isEmpty() && lookupPath.indexOf('.') != -1) {
                for (String extension : this.fileExtensions) {
                    if (this.pathMatcher.match(pattern + extension, lookupPath)) {
                        return pattern + extension;
                    }
                }
            } else {
                boolean noSuffix = pattern.indexOf('.') == -1;
                if (noSuffix && this.pathMatcher.match(pattern + WILDCARD_EXTENSION, lookupPath)) {
                    return pattern + WILDCARD_EXTENSION;
                }
            }
        }

        if (this.pathMatcher.match(pattern, lookupPath)) return pattern;

        if (this.useTrailingSlashMatch) {
            if (!pattern.endsWith("/") && this.pathMatcher.match(pattern + "/", lookupPath)) {
                return pattern + "/";
            }
        }
        return null;
    }

    static Set<String> initPatterns(String @Nullable [] patterns) {
        if (!hasPattern(patterns)) return EMPTY_PATH_PATTERN;
        Set<String> result = new LinkedHashSet<>(patterns.length);
        for (String pattern : patterns) {
            if (!pattern.startsWith("/")) {
                pattern = "/" + pattern;
            }
            result.add(pattern);
        }
        return result;
    }

    static boolean hasPattern(String @Nullable [] patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (pattern != null && !pattern.isEmpty()) return true;
            }
        }
        return false;
    }
}
