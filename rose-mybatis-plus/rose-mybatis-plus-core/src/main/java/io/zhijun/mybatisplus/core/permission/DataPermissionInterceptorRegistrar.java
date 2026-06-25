package io.zhijun.mybatisplus.core.permission;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;

import io.zhijun.mybatisplus.core.extension.InnerInterceptorSupport;
import io.zhijun.mybatisplus.core.extension.MybatisPlusInterceptorCustomizer;

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
        InnerInterceptorSupport.addIfAbsent(interceptor, new DataPermissionInterceptor(dataPermissionHandler),
                DataPermissionInterceptor.class);
    }
}
