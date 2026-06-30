package io.zhijun.mybatisplus.core.extension;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;



/**
 * Shared helpers for registering {@link InnerInterceptor} instances once per type.
 */

public final class InnerInterceptorSupport {

    private InnerInterceptorSupport() {}

    public static boolean contains(MybatisPlusInterceptor interceptor, Class<? extends InnerInterceptor> type) {
        for (InnerInterceptor innerInterceptor : interceptor.getInterceptors()) {
            if (type.isInstance(innerInterceptor)) {
                return true;
            }
        }
        return false;
    }

    public static void addIfAbsent(
            MybatisPlusInterceptor interceptor, InnerInterceptor toAdd, Class<? extends InnerInterceptor> type) {
        if (!contains(interceptor, type)) {
            interceptor.addInnerInterceptor(toAdd);
        }
    }
}
