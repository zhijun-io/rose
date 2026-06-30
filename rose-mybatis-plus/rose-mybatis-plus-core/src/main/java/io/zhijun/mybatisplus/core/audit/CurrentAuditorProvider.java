package io.zhijun.mybatisplus.core.audit;

/**
 * Supplies the current auditor for audit field auto-fill.
 */
@FunctionalInterface
public interface CurrentAuditorProvider {

    String getCurrentAuditor();
}
