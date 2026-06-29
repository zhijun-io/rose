package io.zhijun.mybatisplus.core.crypto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field for encryption before persistence and decryption after load.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldEncrypt {

    EncryptAlgorithm algorithm() default EncryptAlgorithm.BASE64;

    /**
     * Secret reference key resolved by {@link EncryptionKeyResolver}.
     */
    String secretRef() default "default";

    Class<? extends FieldEncryptor> encryptor() default FieldEncryptor.class;
}
