package im.status.ethereum.module;

import android.security.keystore.KeyProperties;
import android.security.keystore.KeyGenParameterSpec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

import statusgo.Keychain;

public class AndroidKeychain implements Keychain {
    private static final String KEYSTORE_TYPE = "AndroidKeyStore";
    private static final String CIPHER_MODE = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTE = 16;
    private static final int TAG_SIZE_BITS = 128;

    public void createKey(String auth) throws Exception {
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystore.load(null);
        if (keystore.containsAlias(auth)) {
            throw new Exception("key exists");
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE);
        keyGenerator.init(
                new KeyGenParameterSpec.Builder(auth, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
        keyGenerator.generateKey();
    }

    public byte[] encrypt(String auth, byte[] dec) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null);
        Key key = keyStore.getKey(auth, null);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        SecureRandom secure = new SecureRandom();
        secure.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BITS, iv));
        byte[] encrypted = cipher.doFinal(dec);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv);
        outputStream.write(encrypted);
        outputStream.close();
        return outputStream.toByteArray();
    }

    public byte[] decrypt(String auth, byte[] enc) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null);
        Key key = keyStore.getKey(auth, null);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        ByteArrayInputStream inputStream = new ByteArrayInputStream(enc);
        inputStream.read(iv, 0, iv.length);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BITS, iv));

        CipherInputStream cipherInputStream = new CipherInputStream(
                inputStream, cipher);

        byte[] buffer = new byte[1024];
        while (true) {
            int n = cipherInputStream.read(buffer, 0, buffer.length);
            if (n <= 0) {
                break;
            }
            outputStream.write(buffer, 0, n);
        }
        return outputStream.toByteArray();
    }

    public void deleteKey(String auth) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null);
        keyStore.deleteEntry(auth);
    }

    public long securityLevel() {
        // TODO do a test for security level
        return 2;
    }
}
