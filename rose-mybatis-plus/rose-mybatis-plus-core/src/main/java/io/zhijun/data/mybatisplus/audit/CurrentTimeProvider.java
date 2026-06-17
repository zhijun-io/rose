package io.zhijun.data.mybatisplus.audit;

import java.time.LocalDateTime;

/**
 * Supplies the current time for audit field auto-fill.
 */
@FunctionalInterface
public interface CurrentTimeProvider {

    LocalDateTime now();
}
