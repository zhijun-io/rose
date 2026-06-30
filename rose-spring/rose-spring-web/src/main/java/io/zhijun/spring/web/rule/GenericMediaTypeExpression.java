package io.zhijun.spring.web.rule;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;

import java.util.Map;

/**
 * 通用媒体类型表达式实现，支持可选的取反。
 */
public class GenericMediaTypeExpression implements MediaTypeExpression, Comparable<GenericMediaTypeExpression> {

    private final MediaType mediaType;

    private final boolean isNegated;

    public GenericMediaTypeExpression(String expression) {
        if (expression.startsWith("!")) {
            this.isNegated = true;
            expression = expression.substring(1);
        } else {
            this.isNegated = false;
        }
        this.mediaType = MediaType.parseMediaType(expression);
    }

    GenericMediaTypeExpression(MediaType mediaType, boolean negated) {
        this.mediaType = mediaType;
        this.isNegated = negated;
    }

    @Override
    public MediaType getMediaType() {
        return this.mediaType;
    }

    @Override
    public boolean isNegated() {
        return this.isNegated;
    }

    protected boolean matchParameters(MediaType mediaType) {
        return matchParameters(this.getMediaType().getParameters(), mediaType.getParameters());
    }

    static boolean matchParameters(Map<String, String> sourceParameters, Map<String, String> targetParameters) {
        for (Map.Entry<String, String> entry : sourceParameters.entrySet()) {
            String name = entry.getKey();
            String s1 = entry.getValue();
            String s2 = targetParameters.get(name);
            if (s1 != null && !s1.isEmpty() && s2 != null && !s1.equalsIgnoreCase(s2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(GenericMediaTypeExpression other) {
        return MediaType.SPECIFICITY_COMPARATOR.compare(this.getMediaType(), other.getMediaType());
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        GenericMediaTypeExpression otherExpr = (GenericMediaTypeExpression) other;
        return this.mediaType.equals(otherExpr.mediaType) && this.isNegated == otherExpr.isNegated;
    }

    @Override
    public int hashCode() {
        return this.mediaType.hashCode();
    }

    @Override
    public String toString() {
        if (this.isNegated) return '!' + this.mediaType.toString();
        return this.mediaType.toString();
    }

    public static GenericMediaTypeExpression of(String expression) {
        return new GenericMediaTypeExpression(expression);
    }
}
