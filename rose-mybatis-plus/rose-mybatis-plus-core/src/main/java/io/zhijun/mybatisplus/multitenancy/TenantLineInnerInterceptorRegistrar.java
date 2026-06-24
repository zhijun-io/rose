package io.zhijun.mybatisplus.multitenancy;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;

import io.zhijun.mybatisplus.extension.MybatisPlusInterceptorCustomizer;

/**
 * Registers a {@link TenantLineInnerInterceptor} on the {@link MybatisPlusInterceptor}.
 */
public final class TenantLineInnerInterceptorRegistrar implements MybatisPlusInterceptorCustomizer {

    private final TenantLineHandler tenantLineHandler;

    public TenantLineInnerInterceptorRegistrar(TenantLineHandler tenantLineHandler) {
        this.tenantLineHandler = tenantLineHandler;
    }

    @Override
    public void customize(MybatisPlusInterceptor interceptor) {
        if (containsTenantLineInnerInterceptor(interceptor)) {
            return;
        }
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));
    }

    private static boolean containsTenantLineInnerInterceptor(MybatisPlusInterceptor interceptor) {
        for (InnerInterceptor innerInterceptor : interceptor.getInterceptors()) {
            if (innerInterceptor instanceof TenantLineInnerInterceptor) {
                return true;
            }
        }
        return false;
    }

}
