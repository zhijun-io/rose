package io.zhijun.mybatisplus.observation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class SqlObservationInterceptorTests {

    private static final Configuration CONFIG = new Configuration();

    @Test
    void shouldRecordTimerOnSuccessfulExecution() throws Throwable {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SqlObservationInterceptor interceptor = new SqlObservationInterceptor(null, meterRegistry);

        MappedStatement ms = buildMappedStatement(SqlCommandType.SELECT, "io.example.UserMapper.selectById");
        Invocation invocation = new TestInvocation(ms, "ok");

        interceptor.intercept(invocation);

        assertThat(meterRegistry.find("db.sql.execution").timers()).hasSize(1);
        Timer timer = meterRegistry.find("db.sql.execution").timer();
        assertThat(timer.getId().getTag("operation")).isEqualTo("io.example.UserMapper.selectById");
        assertThat(timer.getId().getTag("sql.operation")).isEqualTo("SELECT");
    }

    @Test
    void shouldNotFailWhenBothTracerAndMeterRegistryAreNull() throws Throwable {
        SqlObservationInterceptor interceptor = new SqlObservationInterceptor(null, null);
        MappedStatement ms = buildMappedStatement(SqlCommandType.INSERT, "io.example.UserMapper.insert");
        Invocation invocation = new TestInvocation(ms, "ok");

        Object result = interceptor.intercept(invocation);
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void shouldPropagateExceptionAndStillRecordTimer() throws Throwable {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SqlObservationInterceptor interceptor = new SqlObservationInterceptor(null, meterRegistry);

        RuntimeException failure = new RuntimeException("SQL error");
        MappedStatement ms = buildMappedStatement(SqlCommandType.DELETE, "io.example.UserMapper.delete");
        Invocation invocation = new TestInvocation(ms, failure);

        assertThatThrownBy(() -> interceptor.intercept(invocation))
                .isSameAs(failure);

        assertThat(meterRegistry.find("db.sql.execution").timers()).hasSize(1);
        assertThat(meterRegistry.find("db.sql.execution").timer().getId().getTag("sql.operation"))
                .isEqualTo("DELETE");
    }

    private static MappedStatement buildMappedStatement(SqlCommandType commandType, String id) {
        SqlSource sqlSource = parameterObject -> new BoundSql(CONFIG, "SELECT 1", null, null);
        return new MappedStatement.Builder(CONFIG, id, sqlSource, commandType).build();
    }

    /**
     * Minimal Invocation stub that returns a fixed result or throws a fixed failure.
     */
    static class TestInvocation extends Invocation {

        private final Object[] args;
        private final Object result;
        private final RuntimeException failure;

        TestInvocation(MappedStatement ms, Object result) {
            super(new Object(), Object.class.getMethods()[0], new Object[0]);
            this.args = new Object[]{ms, new Object()};
            this.result = result;
            this.failure = null;
        }

        TestInvocation(MappedStatement ms, RuntimeException failure) {
            super(new Object(), Object.class.getMethods()[0], new Object[0]);
            this.args = new Object[]{ms, new Object()};
            this.result = null;
            this.failure = failure;
        }

        @Override
        public Object proceed() {
            if (failure != null) {
                throw failure;
            }
            return result;
        }

        @Override
        public Object[] getArgs() {
            return args;
        }

        @Override
        public Object getTarget() {
            return new Object();
        }

        @Override
        public java.lang.reflect.Method getMethod() {
            return Object.class.getMethods()[0];
        }
    }
}
