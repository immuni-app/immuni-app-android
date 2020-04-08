package com.bendingspoons.base.crypto

/**
 * In this class we can change the crypt algorithms we use.
 */
object CryptoAlgorithmWrapper {

    val prefix = AESUtils.CRYPTO_KEY_PREFIX

    fun encrypt(cleartext: String?): String? {
        return if (cleartext == null) null else AESUtils.encrypt(cleartext)
        //return  Base64.encodeToString(cleartext.getBytes(), Base64.NO_WRAP);
    }

    fun decrypt(encrypted: String?): String? {
        return if (encrypted == null) null else AESUtils.decrypt(encrypted)
        //return new String(Base64.decode(encrypted.getBytes(), Base64.NO_WRAP ));
    }
}
