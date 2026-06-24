package io.zhijun.mybatisplus.core.audit;

import java.time.LocalDateTime;

/**
 * Supplies the current time for audit field auto-fill.
 */
@FunctionalInterface
public interface CurrentTimeProvider {

    LocalDateTime now();
}
