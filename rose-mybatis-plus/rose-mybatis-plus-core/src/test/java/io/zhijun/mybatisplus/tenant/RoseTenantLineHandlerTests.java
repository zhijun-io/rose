package io.zhijun.mybatisplus.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;

class RoseTenantLineHandlerTests {

    @Test
    void shouldResolveTenantIdFromSupplier() {
        RoseTenantLineHandler handler = new RoseTenantLineHandler(() -> "acme", "tenant_id", java.util.Collections.<String>emptySet());

        assertThat(handler.getTenantId().toString()).isEqualTo("'acme'");
        assertThat(handler.getTenantIdColumn()).isEqualTo("tenant_id");
        assertThat(handler.ignoreTable("sys_user")).isFalse();
    }

    @Test
    void shouldIgnoreConfiguredTables() {
        RoseTenantLineHandler handler = new RoseTenantLineHandler(() -> "acme", "tenant_id",
                java.util.Collections.singleton("sys_dict"));

        assertThat(handler.ignoreTable("sys_dict")).isTrue();
        assertThat(handler.ignoreTable("sys_user")).isFalse();
    }

}
