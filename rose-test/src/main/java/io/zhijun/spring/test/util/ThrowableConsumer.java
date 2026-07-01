package io.zhijun.spring.test.util;

/**
 * A consumer that allows throwing checked exceptions.
 *
 * @param <T> the type of the input
 */
@FunctionalInterface
public interface ThrowableConsumer<T> {

    void accept(T t) throws Throwable;
}
