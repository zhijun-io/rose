package io.zhijun.mybatisplus.permission;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;

/**
 * Registers {@link DataPermissionInterceptor} on an existing {@link MybatisPlusInterceptor}.
 */
public final class DataPermissionInnerInterceptorRegistrar implements BeanPostProcessor {

    private final DataPermissionHandler dataPermissionHandler;

    public DataPermissionInnerInterceptorRegistrar(DataPermissionHandler dataPermissionHandler) {
        this.dataPermissionHandler = dataPermissionHandler;
    }

    @Override
    public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) throws BeansException {
        if (!(bean instanceof MybatisPlusInterceptor)) {
            return bean;
        }
        MybatisPlusInterceptor interceptor = (MybatisPlusInterceptor) bean;
        if (containsDataPermissionInterceptor(interceptor)) {
            return bean;
        }
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(dataPermissionHandler));
        return bean;
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
