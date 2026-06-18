package io.zhijun.mybatisplus.crypto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Reflective helper for {@link FieldEncrypt} processing.
 */
public final class FieldEncryptProcessor {

    private static final Map<Class<?>, List<EncryptedField>> CACHE = new ConcurrentHashMap<Class<?>, List<EncryptedField>>();

    private FieldEncryptProcessor() {}

    public static void processWrite(Object entity, FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        if (entity == null) {
            return;
        }
        for (EncryptedField encryptedField : resolveFields(entity.getClass())) {
            Object rawValue = ReflectionUtils.getField(encryptedField.field, entity);
            if (rawValue == null) {
                continue;
            }
            String secret = keyResolver.resolve(encryptedField.annotation.secretRef());
            String encrypted = encryptor.encrypt(encryptedField.annotation.algorithm(), secret, rawValue);
            ReflectionUtils.setField(encryptedField.field, entity, encrypted);
        }
    }

    public static void processRead(Object entity, FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        if (entity == null) {
            return;
        }
        for (EncryptedField encryptedField : resolveFields(entity.getClass())) {
            Object storedValue = ReflectionUtils.getField(encryptedField.field, entity);
            if (storedValue == null) {
                continue;
            }
            String secret = keyResolver.resolve(encryptedField.annotation.secretRef());
            String decrypted = encryptor.decrypt(encryptedField.annotation.algorithm(), secret, storedValue);
            ReflectionUtils.setField(encryptedField.field, entity, decrypted);
        }
    }

    public static boolean shouldProcess(SqlCommandType commandType) {
        return commandType == SqlCommandType.INSERT
                || commandType == SqlCommandType.UPDATE
                || commandType == SqlCommandType.SELECT;
    }

    public static void processParameter(Object parameter, SqlCommandType commandType,
            FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        if (!shouldProcess(commandType) || parameter == null) {
            return;
        }
        if (commandType == SqlCommandType.SELECT) {
            return;
        }
        if (parameter instanceof Map<?, ?>) {
            for (Object value : ((Map<?, ?>) parameter).values()) {
                processWrite(value, encryptor, keyResolver);
            }
            return;
        }
        processWrite(parameter, encryptor, keyResolver);
    }

    public static void processResult(@Nullable Object result, FieldEncryptor encryptor,
            EncryptionKeyResolver keyResolver) {
        if (result == null) {
            return;
        }
        if (result instanceof List<?>) {
            for (Object item : (List<?>) result) {
                processRead(item, encryptor, keyResolver);
            }
            return;
        }
        processRead(result, encryptor, keyResolver);
    }

    private static List<EncryptedField> resolveFields(Class<?> type) {
        List<EncryptedField> cached = CACHE.get(type);
        if (cached != null) {
            return cached;
        }
        List<EncryptedField> fields = new ArrayList<EncryptedField>();
        Class<?> current = type;
        while (current != null && !Object.class.equals(current)) {
            for (Field field : current.getDeclaredFields()) {
                FieldEncrypt annotation = field.getAnnotation(FieldEncrypt.class);
                if (annotation == null) {
                    continue;
                }
                ReflectionUtils.makeAccessible(field);
                fields.add(new EncryptedField(field, annotation));
            }
            current = current.getSuperclass();
        }
        List<EncryptedField> immutable = Collections.unmodifiableList(fields);
        CACHE.put(type, immutable);
        return immutable;
    }

    private static final class EncryptedField {
        private final Field field;
        private final FieldEncrypt annotation;

        private EncryptedField(Field field, FieldEncrypt annotation) {
            this.field = field;
            this.annotation = annotation;
        }
    }
}
