package pt.tecnico.sirsproject.security;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SymmetricKey {
    public static String encrypt(String data, String aesKeyString) {
        byte[] decodedKey = Base64.getDecoder().decode(aesKeyString);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        // AES defaults to AES/ECB/PKCS5Padding
        Cipher aesCipher = null;
        byte[] byteCipherText = new byte[0];

        try {
            aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] iv_array = new byte[aesCipher.getBlockSize()];
            IvParameterSpec iv = new IvParameterSpec(iv_array);

            aesCipher.init(Cipher.ENCRYPT_MODE, originalKey, iv);
            byteCipherText = aesCipher.doFinal(data.getBytes());

            System.out.println("CipherText: " + Base64.getEncoder().encodeToString(byteCipherText));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(byteCipherText);
    }

    public static String decrypt(String encrypted_data, String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        byte[] decodedData = Base64.getDecoder().decode(encrypted_data);

        SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, "AES");
        String unencrypted_data = "";
        try {
            Cipher aes_cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] iv_array = new byte[aes_cipher.getBlockSize()];
            IvParameterSpec iv = new IvParameterSpec(iv_array);

            aes_cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

            unencrypted_data = new String(aes_cipher.doFinal(decodedData),
                    StandardCharsets.UTF_8);
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return unencrypted_data;
    }

    private SecretKey createAESKey() {
        SecureRandom secureRandom = new SecureRandom();

        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert keyGenerator != null;
        keyGenerator.init(256, secureRandom);
        return keyGenerator.generateKey();
    }

    public String getEncodedSymmetricKey() {
        SecretKey AESKey = createAESKey(); // 32 Byte = 256 bit Key
        byte[] encodedAES = AESKey.getEncoded();
        String AES_base64 = Base64.getEncoder().encodeToString(encodedAES);
        return AES_base64;
    }
}
