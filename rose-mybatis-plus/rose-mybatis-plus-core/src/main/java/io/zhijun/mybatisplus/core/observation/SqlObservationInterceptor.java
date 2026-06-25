package io.zhijun.mybatisplus.core.observation;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import io.zhijun.annotation.Incubating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MyBatis {@link Interceptor} that records SQL execution as OpenTelemetry spans and
 * Micrometer timers.
 * <p>
 * Spans carry the following attributes (OpenTelemetry semantic conventions for database
 * client calls):
 * <ul>
 *     <li>{@code db.operation} — the mapper method name</li>
 *     <li>{@code db.statement} — the SQL text (truncated)</li>
 *     <li>{@code db.sql.operation} — SELECT / INSERT / UPDATE / DELETE</li>
 * </ul>
 * <p>
 * Metrics are recorded as a {@link Timer} named {@code db.sql.execution} with tags
 * {@code operation} (mapper method) and {@code sql.operation} (SQL command type).
 * <p>
 * This interceptor is conditionally registered when an {@link Tracer} or
 * {@link MeterRegistry} bean is available. It is a standalone MyBatis interceptor
 * (not an {@code InnerInterceptor}) so that it can wrap the full execution in a
 * {@code try/finally} block and capture both before and after states.
 *
 * @since 0.0.1
 */
@Incubating
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        org.apache.ibatis.cache.CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlObservationInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SqlObservationInterceptor.class);

    /**
     * Maximum SQL text length captured in span attributes to avoid excessive payload.
     */
    private static final int MAX_SQL_LENGTH = 4096;

    private static final String SPAN_NAME_PREFIX = "DB ";

    private static final String METER_NAME = "db.sql.execution";

    private static final String TAG_OPERATION = "operation";

    private static final String TAG_SQL_OPERATION = "sql.operation";

    private final Tracer tracer;

    private final MeterRegistry meterRegistry;

    /**
     * Threshold beyond which a SQL execution is considered slow and logged at WARN level.
     * {@code null} or {@code <= 0} disables slow-query detection.
     */
    private final long slowQueryThresholdMillis;

    /**
     * Construct with an explicit {@link Tracer} and {@link MeterRegistry}.
     *
     * @param tracer       the OTel tracer; if {@code null}, spans are not created
     * @param meterRegistry the Micrometer registry; if {@code null}, metrics are not recorded
     */
    public SqlObservationInterceptor(Tracer tracer, MeterRegistry meterRegistry) {
        this(tracer, meterRegistry, 0);
    }

    /**
     * Construct with slow-query threshold.
     *
     * @param tracer       the OTel tracer; if {@code null}, spans are not created
     * @param meterRegistry the Micrometer registry; if {@code null}, metrics are not recorded
     * @param slowQueryThresholdMillis threshold in millis; {@code <= 0} disables detection
     */
    public SqlObservationInterceptor(Tracer tracer, MeterRegistry meterRegistry,
            long slowQueryThresholdMillis) {
        this.tracer = tracer;
        this.meterRegistry = meterRegistry;
        this.slowQueryThresholdMillis = slowQueryThresholdMillis;
    }

    /**
     * Construct for metrics-only observation when no {@link Tracer} bean is available.
     *
     * @param meterRegistry the Micrometer registry; if {@code null}, metrics are not recorded
     */
    public SqlObservationInterceptor(MeterRegistry meterRegistry) {
        this(null, meterRegistry, 0);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        SqlCommandType commandType = mappedStatement.getSqlCommandType();
        String operation = mappedStatement.getId();
        String sqlText = extractSql(invocation, mappedStatement);

        Span span = null;
        Scope scope = null;
        long startNanos = System.nanoTime();

        if (tracer != null) {
            span = tracer.spanBuilder(SPAN_NAME_PREFIX + commandType.name().toLowerCase())
                    .setAttribute("db.operation", operation)
                    .setAttribute("db.sql.operation", commandType.name())
                    .startSpan();
            scope = span.makeCurrent();
        }

        try {
            return invocation.proceed();
        } catch (Throwable failure) {
            if (span != null) {
                span.recordException(failure);
                span.setAttribute("otel.status_code", "ERROR");
            }
            throw failure;
        } finally {
            long elapsedNanos = System.nanoTime() - startNanos;
            if (slowQueryThresholdMillis > 0) {
                long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
                if (elapsedMillis >= slowQueryThresholdMillis) {
                    logger.warn("Slow SQL detected: {} took {}ms (threshold: {}ms)",
                            operation, elapsedMillis, slowQueryThresholdMillis);
                    if (span != null) {
                        span.setAttribute("db.slow_query", true);
                    }
                }
            }
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                if (sqlText != null) {
                    span.setAttribute("db.statement",
                            sqlText.length() > MAX_SQL_LENGTH
                                    ? sqlText.substring(0, MAX_SQL_LENGTH)
                                    : sqlText);
                }
                span.end();
            }
            if (meterRegistry != null) {
                Timer.builder(METER_NAME)
                        .tag(TAG_OPERATION, operation)
                        .tag(TAG_SQL_OPERATION, commandType.name())
                        .register(meterRegistry)
                        .record(elapsedNanos, TimeUnit.NANOSECONDS);
            }
        }
    }

    private String extractSql(Invocation invocation, MappedStatement mappedStatement) {
        try {
            Object[] args = invocation.getArgs();
            if (args.length >= 6 && args[5] instanceof BoundSql) {
                return ((BoundSql) args[5]).getSql();
            }
            return mappedStatement.getBoundSql(args[1]).getSql();
        } catch (Exception ex) {
            logger.debug("Failed to extract SQL for observation on {}", mappedStatement.getId(), ex);
            return null;
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        // no-op
    }
}
