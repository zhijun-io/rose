package io.zhijun.mybatisplus.permission;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;

import io.zhijun.mybatisplus.extension.MybatisPlusInterceptorCustomizer;

/**
 * Registers a {@link DataPermissionInterceptor} on the {@link MybatisPlusInterceptor}
 * using the configured {@link DataPermissionHandler}.
 *
 * @see MybatisPlusInterceptorCustomizer
 */
public final class DataPermissionInterceptorRegistrar implements MybatisPlusInterceptorCustomizer {

    private final DataPermissionHandler dataPermissionHandler;

    public DataPermissionInterceptorRegistrar(DataPermissionHandler dataPermissionHandler) {
        this.dataPermissionHandler = dataPermissionHandler;
    }

    @Override
    public void customize(MybatisPlusInterceptor interceptor) {
        if (containsDataPermissionInterceptor(interceptor)) {
            return;
        }
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(dataPermissionHandler));
    }

    private static boolean containsDataPermissionInterceptor(MybatisPlusInterceptor interceptor) {
        for (InnerInterceptor innerInterceptor : interceptor.getInterceptors()) {
            if (innerInterceptor instanceof DataPermissionInterceptor) {
                return true;
            }
        }
        return false;
    }
}
