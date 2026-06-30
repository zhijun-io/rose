package io.zhijun.spring.web.rule;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.StringJoiner;

/**
 * {@link WebRequestRule} 抽象基类，提供 equals/hashCode/toString 公共实现。
 */
public abstract class AbstractWebRequestRule<T> implements WebRequestRule {

    public boolean isEmpty() {
        return getContent().isEmpty();
    }

    protected abstract Collection<T> getContent();

    protected abstract String getToStringInfix();

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return getContent().equals(((AbstractWebRequestRule<?>) other).getContent());
    }

    @Override
    public int hashCode() {
        return getContent().hashCode();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(getToStringInfix(), "[", "]");
        for (Object expression : getContent()) {
            joiner.add(expression.toString());
        }
        return joiner.toString();
    }
}
