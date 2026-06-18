package io.zhijun.mybatisplus.audit;

import org.springframework.lang.Nullable;

/**
 * Supplies the current auditor for audit field auto-fill.
 */
@FunctionalInterface
public interface CurrentAuditorProvider {

    @Nullable
    String getCurrentAuditor();
}
