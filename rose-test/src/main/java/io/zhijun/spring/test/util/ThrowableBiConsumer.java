package io.zhijun.spring.test.util;

/**
 * A bi-consumer that allows throwing checked exceptions.
 *
 * @param <T> the first input type
 * @param <U> the second input type
 */
@FunctionalInterface
public interface ThrowableBiConsumer<T, U> {

    void accept(T t, U u) throws Throwable;
}
