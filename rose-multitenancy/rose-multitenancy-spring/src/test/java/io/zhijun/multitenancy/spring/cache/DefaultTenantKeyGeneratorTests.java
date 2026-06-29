package io.zhijun.multitenancy.spring.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.core.context.TenantContext;
import io.zhijun.multitenancy.core.exception.TenantNotFoundException;

class DefaultTenantKeyGeneratorTests {

    private static final Method DUMMY_METHOD;

    static {
        try {
            DUMMY_METHOD = DefaultTenantKeyGeneratorTests.class.getDeclaredMethod("dummyMethod", String.class);
        } catch (NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private final DefaultTenantKeyGenerator keyGenerator = new DefaultTenantKeyGenerator();

    @Test
    void whenTenantContextDefinedThenGenerateCacheKey() {
        Object[] objectToCache = new Object[] {"something"};

        TenantContext.where("tenant1").run(() -> {
            Object key1 = generateCacheKey(objectToCache);
            Object key2 = generateCacheKey(objectToCache);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());

            TenantContext.where("tenant2").run(() -> {
                Object key3 = generateCacheKey(objectToCache);
                assertThat(key1.hashCode()).isNotEqualTo(key3.hashCode());
            });
        });
    }

    @Test
    void whenTenantContextNotDefinedThenThrow() {
        assertThatThrownBy(() -> generateCacheKey(new Object[] {"something"}))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessageContaining("No tenant found in the current context");
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String argument) {}

    private Object generateCacheKey(Object[] arguments) {
        return keyGenerator.generate(this, DUMMY_METHOD, arguments);
    }
}
