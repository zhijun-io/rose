package io.zhijun.spring.web.metadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 不可变值对象，描述一个 Web 端点的 HTTP 映射信息（路径、方法、参数、头部、消费/生产类型）。
 * <p>
 * 与第三方同类库的区别：
 * <ul>
 *   <li>无 {@code Kind} 枚举 — 避免耦合 Jakarta EE 类型</li>
 *   <li>无 {@code id} / {@code negated} / {@code attributes} — 只保留 HTTP 路由语义</li>
 *   <li>无 {@code toJSON()} / {@code toExpression()} — 序列化由 Spring/Jackson 完成</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class WebEndpointMapping {

    /** 未知来源标记 */
    public static final Object UNKNOWN_SOURCE = new Object();

    private final Object endpoint;

    private final Object source;

    private final String[] patterns;

    private final String[] methods;

    private final String[] params;

    private final String[] headers;

    private final String[] consumes;

    private final String[] produces;

    private WebEndpointMapping(Builder builder) {
        this.endpoint = builder.endpoint;
        this.source = builder.source;
        this.patterns = toArray(builder.patterns);
        this.methods = toArray(builder.methods);
        this.params = toArray(builder.params);
        this.headers = toArray(builder.headers);
        this.consumes = toArray(builder.consumes);
        this.produces = toArray(builder.produces);
    }

    public Object getEndpoint() {
        return endpoint;
    }

    public Object getSource() {
        return source;
    }

    public String[] getPatterns() {
        return patterns;
    }

    public String[] getMethods() {
        return methods;
    }

    public String[] getParams() {
        return params;
    }

    public String[] getHeaders() {
        return headers;
    }

    public String[] getConsumes() {
        return consumes;
    }

    public String[] getProduces() {
        return produces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebEndpointMapping that = (WebEndpointMapping) o;
        return Arrays.equals(patterns, that.patterns)
                && Arrays.equals(methods, that.methods)
                && Arrays.equals(params, that.params)
                && Arrays.equals(headers, that.headers)
                && Arrays.equals(consumes, that.consumes)
                && Arrays.equals(produces, that.produces)
                && Objects.equals(endpoint, that.endpoint)
                && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(endpoint, source);
        result = 31 * result + Arrays.hashCode(patterns);
        result = 31 * result + Arrays.hashCode(methods);
        result = 31 * result + Arrays.hashCode(params);
        result = 31 * result + Arrays.hashCode(headers);
        result = 31 * result + Arrays.hashCode(consumes);
        result = 31 * result + Arrays.hashCode(produces);
        return result;
    }

    @Override
    public String toString() {
        return "WebEndpointMapping{" +
                "patterns=" + Arrays.toString(patterns) +
                ", methods=" + Arrays.toString(methods) +
                ", params=" + Arrays.toString(params) +
                ", headers=" + Arrays.toString(headers) +
                ", consumes=" + Arrays.toString(consumes) +
                ", produces=" + Arrays.toString(produces) +
                ", endpoint=" + endpoint +
                ", source=" + source +
                '}';
    }

    private static String[] toArray(Set<String> set) {
        return set.toArray(new String[0]);
    }

    // ---- Builder ----

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Object endpoint;

        private Object source = UNKNOWN_SOURCE;

        private final Set<String> patterns = new LinkedHashSet<>();

        private final Set<String> methods = new LinkedHashSet<>();

        private final Set<String> params = new LinkedHashSet<>();

        private final Set<String> headers = new LinkedHashSet<>();

        private final Set<String> consumes = new LinkedHashSet<>();

        private final Set<String> produces = new LinkedHashSet<>();

        public Builder endpoint(Object endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder source(Object source) {
            this.source = source;
            return this;
        }

        public Builder pattern(String pattern) {
            this.patterns.add(pattern);
            return this;
        }

        public Builder patterns(String... patterns) {
            Collections.addAll(this.patterns, patterns);
            return this;
        }

        public Builder patterns(Collection<String> patterns) {
            this.patterns.addAll(patterns);
            return this;
        }

        public Builder method(String method) {
            this.methods.add(method);
            return this;
        }

        public Builder methods(String... methods) {
            Collections.addAll(this.methods, methods);
            return this;
        }

        public Builder methods(Collection<String> methods) {
            this.methods.addAll(methods);
            return this;
        }

        public Builder param(String param) {
            this.params.add(param);
            return this;
        }

        public Builder params(String... params) {
            Collections.addAll(this.params, params);
            return this;
        }

        public Builder params(Collection<String> params) {
            this.params.addAll(params);
            return this;
        }

        public Builder header(String header) {
            this.headers.add(header);
            return this;
        }

        public Builder headers(String... headers) {
            Collections.addAll(this.headers, headers);
            return this;
        }

        public Builder headers(Collection<String> headers) {
            this.headers.addAll(headers);
            return this;
        }

        public Builder consume(String consume) {
            this.consumes.add(consume);
            return this;
        }

        public Builder consumes(String... consumes) {
            Collections.addAll(this.consumes, consumes);
            return this;
        }

        public Builder consumes(Collection<String> consumes) {
            this.consumes.addAll(consumes);
            return this;
        }

        public Builder produce(String produce) {
            this.produces.add(produce);
            return this;
        }

        public Builder produces(String... produces) {
            Collections.addAll(this.produces, produces);
            return this;
        }

        public Builder produces(Collection<String> produces) {
            this.produces.addAll(produces);
            return this;
        }

        public WebEndpointMapping build() {
            return new WebEndpointMapping(this);
        }
    }
}
