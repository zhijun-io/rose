package io.zhijun.mybatisplus.core.crypto;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * Encrypts annotated fields before write and decrypts after read.
 */
@Intercepts({
    @Signature(
            type = Executor.class,
            method = "update",
            args = {MappedStatement.class, Object.class}),
    @Signature(
            type = Executor.class,
            method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(
            type = Executor.class,
            method = "query",
            args = {
                MappedStatement.class,
                Object.class,
                RowBounds.class,
                ResultHandler.class,
                CacheKey.class,
                BoundSql.class
            })
})
public class FieldEncryptInterceptor implements Interceptor {

    private final FieldEncryptor encryptor;

    private final EncryptionKeyResolver keyResolver;

    public FieldEncryptInterceptor(FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        this.encryptor = encryptor;
        this.keyResolver = keyResolver;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        SqlCommandType commandType = mappedStatement.getSqlCommandType();
        if (commandType == SqlCommandType.INSERT || commandType == SqlCommandType.UPDATE) {
            Runnable restore = FieldEncryptProcessor.processParameter(args[1], commandType, encryptor, keyResolver);
            try {
                return invocation.proceed();
            } finally {
                restore.run();
            }
        }
        if (commandType == SqlCommandType.SELECT) {
            Runnable restore = FieldEncryptProcessor.processParameter(args[1], commandType, encryptor, keyResolver);
            try {
                Object result = invocation.proceed();
                FieldEncryptProcessor.processResult(result, encryptor, keyResolver);
                return result;
            } finally {
                restore.run();
            }
        }
        return invocation.proceed();
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
