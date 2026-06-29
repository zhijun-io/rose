package io.zhijun.mybatisplus.core.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;

import org.junit.jupiter.api.Test;

class TenantLineInnerInterceptorRegistrarTests {

    @Test
    void shouldRegisterTenantLineInnerInterceptor() {
        RoseTenantLineHandler handler =
                new RoseTenantLineHandler(() -> "acme", "tenant_id", Collections.emptySet());
        TenantLineInnerInterceptorRegistrar registrar = new TenantLineInnerInterceptorRegistrar(handler);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        registrar.customize(interceptor);

        assertThat(interceptor.getInterceptors()).hasSize(1);
        assertThat(interceptor.getInterceptors().get(0)).isInstanceOf(TenantLineInnerInterceptor.class);
    }

    @Test
    void shouldNotRegisterDuplicateTenantLineInnerInterceptor() {
        RoseTenantLineHandler handler =
                new RoseTenantLineHandler(() -> "acme", "tenant_id", Collections.emptySet());
        TenantLineInnerInterceptorRegistrar registrar = new TenantLineInnerInterceptorRegistrar(handler);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(handler));

        registrar.customize(interceptor);

        assertThat(interceptor.getInterceptors()).hasSize(1);
    }
}
