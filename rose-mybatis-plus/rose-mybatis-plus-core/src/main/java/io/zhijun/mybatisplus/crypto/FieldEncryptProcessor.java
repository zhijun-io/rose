package io.zhijun.mybatisplus.crypto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.mapping.SqlCommandType;

/**
 * Reflective helper for {@link FieldEncrypt} processing.
 */
public final class FieldEncryptProcessor {

    private static final Map<Class<?>, List<EncryptedField>> CACHE = new ConcurrentHashMap<Class<?>, List<EncryptedField>>();

    private FieldEncryptProcessor() {}

    public static void processWrite(Object entity, FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        encryptFields(entity, encryptor, keyResolver);
    }

    public static void processRead(Object entity, FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        if (entity == null) {
            return;
        }
        for (EncryptedField encryptedField : resolveFields(entity.getClass())) {
            Object storedValue = getField(encryptedField.field, entity);
            if (storedValue == null) {
                continue;
            }
            String secret = keyResolver.resolve(encryptedField.annotation.secretRef());
            String decrypted = encryptor.decrypt(encryptedField.annotation.algorithm(), secret, storedValue);
            setField(encryptedField.field, entity, decrypted);
        }
    }

    /**
     * Encrypts every {@link FieldEncrypt}-annotated field on {@code entity} in place and returns
     * a {@link Runnable} that restores the original values. Call the returned action after the
     * SQL has executed so the caller never observes ciphertext on the entity.
     */
    private static Runnable encryptFields(Object entity, FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        if (entity == null) {
            return () -> {};
        }
        List<EncryptedField> encryptedFields = resolveFields(entity.getClass());
        if (encryptedFields.isEmpty()) {
            return () -> {};
        }
        List<Runnable> restores = new ArrayList<Runnable>();
        for (EncryptedField encryptedField : encryptedFields) {
            Object rawValue = getField(encryptedField.field, entity);
            if (rawValue == null) {
                continue;
            }
            String secret = keyResolver.resolve(encryptedField.annotation.secretRef());
            String encrypted = encryptor.encrypt(encryptedField.annotation.algorithm(), secret, rawValue);
            setField(encryptedField.field, entity, encrypted);
            restores.add(() -> setField(encryptedField.field, entity, rawValue));
        }
        return () -> {
            for (Runnable restore : restores) {
                restore.run();
            }
        };
    }

    public static boolean shouldProcess(SqlCommandType commandType) {
        return commandType == SqlCommandType.INSERT
                || commandType == SqlCommandType.UPDATE
                || commandType == SqlCommandType.SELECT;
    }

    /**
     * Encrypts annotated fields on the parameter (including SELECT query conditions so that
     * {@code WHERE} clauses match stored ciphertext) and returns a {@link Runnable} that restores
     * the original plaintext values after the SQL has executed.
     */
    public static Runnable processParameter(Object parameter, SqlCommandType commandType,
            FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
        if (!shouldProcess(commandType) || parameter == null) {
            return () -> {};
        }
        List<Runnable> restores = new ArrayList<Runnable>();
        if (parameter instanceof Map<?, ?>) {
            for (Object value : ((Map<?, ?>) parameter).values()) {
                restores.add(encryptFields(value, encryptor, keyResolver));
            }
        } else {
            restores.add(encryptFields(parameter, encryptor, keyResolver));
        }
        return () -> {
            for (Runnable restore : restores) {
                restore.run();
            }
        };
    }

    public static void processResult(Object result, FieldEncryptor encryptor, EncryptionKeyResolver keyResolver) {
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
                makeAccessible(field);
                fields.add(new EncryptedField(field, annotation));
            }
            current = current.getSuperclass();
        }
        List<EncryptedField> immutable = Collections.unmodifiableList(fields);
        CACHE.put(type, immutable);
        return immutable;
    }

    private static Object getField(Field field, Object target) {
        try {
            makeAccessible(field);
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Could not read field " + field, ex);
        }
    }

    private static void setField(Field field, Object target, Object value) {
        try {
            makeAccessible(field);
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Could not set field " + field, ex);
        }
    }

    private static void makeAccessible(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
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
