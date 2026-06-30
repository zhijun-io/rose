package io.zhijun.spring.web.rule;

import org.jspecify.annotations.Nullable;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Objects;

/**
 * 支持 "name=value" 风格表达式的抽象基类。
 */
public abstract class AbstractNameValueExpression<T> implements NameValueExpression<T> {

    protected final String expression;

    protected final boolean caseSensitiveName;

    protected final String name;

    @Nullable
    protected final T value;

    protected final boolean isNegated;

    protected AbstractNameValueExpression(String expression) {
        this(expression, false);
    }

    protected AbstractNameValueExpression(String expression, boolean caseSensitiveName) {
        Objects.requireNonNull(expression, "'expression' must not be null");
        this.expression = expression;
        this.caseSensitiveName = caseSensitiveName;
        int separator = expression.indexOf('=');
        if (separator == -1) {
            this.isNegated = expression.startsWith("!");
            this.name = this.isNegated ? expression.substring(1) : expression;
            this.value = null;
        } else {
            this.isNegated = separator > 0 && expression.charAt(separator - 1) == '!';
            this.name = this.isNegated ? expression.substring(0, separator - 1) : expression.substring(0, separator);
            this.value = parseValue(expression.substring(separator + 1));
        }
    }

    protected final String getExpression() {
        return this.expression;
    }

    protected final boolean isCaseSensitiveName() {
        return this.caseSensitiveName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    @Nullable
    public T getValue() {
        return this.value;
    }

    @Override
    public boolean isNegated() {
        return this.isNegated;
    }

    public final boolean match(NativeWebRequest request) {
        boolean isMatch;
        if (this.value != null) {
            isMatch = matchValue(request);
        } else {
            isMatch = matchName(request);
        }
        return this.isNegated != isMatch;
    }

    protected abstract T parseValue(String valueExpression);

    protected abstract boolean matchName(NativeWebRequest request);

    protected abstract boolean matchValue(NativeWebRequest request);

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AbstractNameValueExpression<?> that = (AbstractNameValueExpression<?>) other;
        return ((isCaseSensitiveName() ? this.name.equals(that.name) : this.name.equalsIgnoreCase(that.name)) &&
                Objects.equals(this.value, that.value) && this.isNegated == that.isNegated);
    }

    @Override
    public int hashCode() {
        int result = (isCaseSensitiveName() ? this.name.hashCode() : this.name.toLowerCase().hashCode());
        result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
        result = 31 * result + (this.isNegated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.value != null) {
            builder.append(this.name);
            if (this.isNegated) builder.append('!');
            builder.append('=');
            builder.append(this.value);
        } else {
            if (this.isNegated) builder.append('!');
            builder.append(this.name);
        }
        return builder.toString();
    }
}
