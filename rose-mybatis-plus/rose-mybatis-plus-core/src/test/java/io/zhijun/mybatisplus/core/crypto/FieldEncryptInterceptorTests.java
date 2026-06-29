package io.zhijun.mybatisplus.core.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.Collections;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FieldEncryptInterceptor}.
 */
class FieldEncryptInterceptorTests {

    private static final Configuration CONFIG = new Configuration();

    private final DefaultFieldEncryptor encryptor = new DefaultFieldEncryptor();

    private final EncryptionKeyResolver keyResolver = secretRef -> null;

    @Test
    void shouldEncryptInsertParameterAndRestoreAfterExecution() throws Throwable {
        SampleEntity entity = new SampleEntity();
        entity.setPhone("13800138000");

        Invocation invocation = buildInvocation(SqlCommandType.INSERT, entity, null);
        FieldEncryptInterceptor interceptor = new FieldEncryptInterceptor(encryptor, keyResolver);

        interceptor.intercept(invocation);

        // After execution, the original plaintext must be restored
        assertThat(entity.getPhone()).isEqualTo("13800138000");
    }

    @Test
    void shouldEncryptUpdateParameterAndRestoreAfterExecution() throws Throwable {
        SampleEntity entity = new SampleEntity();
        entity.setPhone("13900139000");

        Invocation invocation = buildInvocation(SqlCommandType.UPDATE, entity, null);
        FieldEncryptInterceptor interceptor = new FieldEncryptInterceptor(encryptor, keyResolver);

        interceptor.intercept(invocation);

        assertThat(entity.getPhone()).isEqualTo("13900139000");
    }

    @Test
    void shouldDecryptSelectResult() throws Throwable {
        String ciphertext = encryptor.encrypt(EncryptAlgorithm.BASE64, null, "13800138000");
        SampleEntity resultEntity = new SampleEntity();
        resultEntity.setPhone(ciphertext);

        Invocation invocation =
                buildInvocation(SqlCommandType.SELECT, new Object(), Collections.singletonList(resultEntity));
        FieldEncryptInterceptor interceptor = new FieldEncryptInterceptor(encryptor, keyResolver);

        Object result = interceptor.intercept(invocation);

        assertThat(result).isInstanceOf(java.util.List.class);
        @SuppressWarnings("unchecked")
        java.util.List<SampleEntity> list = (java.util.List<SampleEntity>) result;
        assertThat(list.get(0).getPhone()).isEqualTo("13800138000");
    }

    @Test
    void shouldRestoreParameterEvenWhenExecutionThrows() throws Throwable {
        SampleEntity entity = new SampleEntity();
        entity.setPhone("13800138000");

        RuntimeException failure = new RuntimeException("SQL error");
        Invocation invocation = buildInvocation(SqlCommandType.INSERT, entity, failure);
        FieldEncryptInterceptor interceptor = new FieldEncryptInterceptor(encryptor, keyResolver);

        assertThatThrownBy(() -> interceptor.intercept(invocation)).isSameAs(failure);

        // Parameter must be restored even on failure
        assertThat(entity.getPhone()).isEqualTo("13800138000");
    }

    @Test
    void shouldPassthroughDeleteWithoutEncryption() throws Throwable {
        Object parameter = new Object();

        Invocation invocation = buildInvocation(SqlCommandType.DELETE, parameter, "ok");
        FieldEncryptInterceptor interceptor = new FieldEncryptInterceptor(encryptor, keyResolver);

        Object result = interceptor.intercept(invocation);
        assertThat(result).isEqualTo("ok");
    }

    private static Invocation buildInvocation(SqlCommandType commandType, Object parameter, Object result) {
        MappedStatement ms = buildMappedStatement(commandType);
        return new TestInvocation(ms, parameter, result);
    }

    private static MappedStatement buildMappedStatement(SqlCommandType commandType) {
        SqlSource sqlSource = parameterObject -> new org.apache.ibatis.mapping.BoundSql(CONFIG, "SELECT 1", null, null);
        return new MappedStatement.Builder(
                        CONFIG, "io.example.Mapper." + commandType.name().toLowerCase(), sqlSource, commandType)
                .build();
    }

    /**
     * Minimal Invocation stub that returns a fixed result or throws a fixed failure.
     */
    static class TestInvocation extends Invocation {

        private static final Method EXECUTOR_UPDATE_METHOD;

        static {
            try {
                EXECUTOR_UPDATE_METHOD = Executor.class.getMethod("update", MappedStatement.class, Object.class);
            } catch (NoSuchMethodException ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        private final Object[] args;
        private final Object result;
        private final RuntimeException failure;

        TestInvocation(MappedStatement ms, Object parameter, Object result) {
            super(new Object() {}, EXECUTOR_UPDATE_METHOD, new Object[] {ms, parameter});
            this.args = new Object[] {ms, parameter};
            this.result = result;
            this.failure = (result instanceof RuntimeException) ? (RuntimeException) result : null;
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
        public Method getMethod() {
            return EXECUTOR_UPDATE_METHOD;
        }
    }

    static class SampleEntity {
        @FieldEncrypt
        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
