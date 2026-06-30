package io.zhijun.spring.web.metadata;

import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 处理器（Handler）与元数据（Metadata）的配对。
 *
 * @param <H> 处理器类型
 * @param <M> 元数据类型
 * @since 1.0.0
 */
public class HandlerMetadata<H, M> {

    private final H handler;

    private final M metadata;

    public HandlerMetadata(H handler, M metadata) {
        Objects.requireNonNull(handler, "handler must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        this.handler = handler;
        this.metadata = metadata;
    }

    public H getHandler() {
        return handler;
    }

    public M getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandlerMetadata<?, ?> that = (HandlerMetadata<?, ?>) o;
        return Objects.equals(handler, that.handler) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler, metadata);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HandlerMetadata{");
        sb.append("handler=").append(handler);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }
}
