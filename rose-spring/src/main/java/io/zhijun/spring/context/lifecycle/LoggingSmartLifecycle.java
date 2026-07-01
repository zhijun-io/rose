package io.zhijun.spring.context.lifecycle;

import org.slf4j.Logger;
import org.springframework.context.SmartLifecycle;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A {@link SmartLifecycle} implementation that logs lifecycle events for debugging and monitoring purposes.
 * <p>
 * This class extends {@link AbstractSmartLifecycle} and provides logging capabilities for lifecycle methods such as
 * {@link #doStart()} and {@link #doStop()}. The logging is performed using the {@link Logger} interface provided by the
 * MicroSphere logging framework.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <p>
 * To use this class in a Spring application context, simply define it as a bean:
 * </p>
 *
 * <pre>{@code
 * @Bean
 * public LoggingSmartLifecycle loggingSmartLifecycle() {
 *     return new LoggingSmartLifecycle();
 * }
 * }</pre>
 *
 * <h3>Example Output</h3>
 * <p>
 * When the Spring context starts or stops, the following messages will be logged:
 * </p>
 *
 * <pre>
 * doStart()...
 * doStop()...
 * </pre>
 *
 * <h3>Customization</h3>
 * <p>
 * Subclasses can override the lifecycle methods to add custom behavior while retaining the logging functionality.
 * For example:
 * </p>
 *
 * <pre>{@code
 * @Override
 * protected void doStart() {
 *     logger.info("Performing custom startup logic...");
 *     // custom startup logic
 *     super.doStart(); // optional
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractSmartLifecycle
 * @see SmartLifecycle
 * @see Logger
 * @since 1.0.0
 */
public class LoggingSmartLifecycle extends AbstractSmartLifecycle {

    private static final Logger logger = getLogger(LoggingSmartLifecycle.class);

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
