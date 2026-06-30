package io.zhijun.mybatisplus.core.crypto;

import java.lang.annotation.*;

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
