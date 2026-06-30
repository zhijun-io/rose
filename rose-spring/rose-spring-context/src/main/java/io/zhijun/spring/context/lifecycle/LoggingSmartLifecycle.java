package io.zhijun.spring.context.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * 带日志的 {@link SmartLifecycle} 实现，用于调试和监控。
 * <p>
 * （移植自 microsphere-spring {@code LoggingSmartLifecycle}）
 *
 * @see AbstractSmartLifecycle
 * @see SmartLifecycle
 */
public class LoggingSmartLifecycle extends AbstractSmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(LoggingSmartLifecycle.class);

    @Override
    protected void doStart() {
        if (logger.isInfoEnabled()) {
            logger.info("doStart()...");
        }
    }

    @Override
    protected void doStop() {
        if (logger.isInfoEnabled()) {
            logger.info("doStop()...");
        }
    }
}
