package com.secmes.secmessaging;

import android.util.Base64;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class RSAUtils {

    private static final String RSA = "RSA";
    private static final String RSA_KEY_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final int KEY_SIZE = 2048;

    // Claves pública y privada
    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    // Genera un par de claves RSA (pública y privada)
    public static void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Obtiene la clave pública en formato String
    public static String getPublicKey() {
        if (publicKey == null) {
            generateKeyPair();
        }
        return Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
    }

    // Obtiene la clave privada en formato String (solo para el dispositivo que la posee)
    public static String getPrivateKey() {
        if (privateKey == null) {
            generateKeyPair();
        }
        return Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
    }

    // Convierte una clave pública (en Base64) en un objeto RSAPublicKey
    public static PublicKey getPublicKeyFromString(String key) throws Exception {
        byte[] decoded = Base64.decode(key, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        return keyFactory.generatePublic(keySpec);
    }

    // Método para cifrar un mensaje con la clave pública
    public static String encrypt(String data, String publicKeyStr) {
        try {
            PublicKey key = getPublicKeyFromString(publicKeyStr);
            Cipher cipher = Cipher.getInstance(RSA_KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método para descifrar un mensaje con la clave privada
    public static String decrypt(String data, String privateKeyStr) {
        try {
            byte[] decodedKey = Base64.decode(privateKeyStr, Base64.DEFAULT);
            RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance(RSA)
                    .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(decodedKey));

            Cipher cipher = Cipher.getInstance(RSA_KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedData = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método para firmar un mensaje con la clave privada
    public static String sign(String message, String privateKeyStr) {
        try {
            byte[] decodedKey = Base64.decode(privateKeyStr, Base64.DEFAULT);
            RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance(RSA)
                    .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(decodedKey));

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes());
            byte[] signedData = signature.sign();
            return Base64.encodeToString(signedData, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método para verificar la firma de un mensaje con la clave pública
    public static boolean verifySignature(String message, String signatureStr, String publicKeyStr) {
        try {
            PublicKey publicKey = getPublicKeyFromString(publicKeyStr);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(message.getBytes());
            byte[] signatureBytes = Base64.decode(signatureStr, Base64.DEFAULT);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
