package com.secmes.secmessaging;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class RSAUtils {

    // Tamaño de la clave RSA
    private static final int KEY_SIZE = 2048;

    // Genera un par de claves RSA (pública y privada)
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    // Convierte la clave pública a una cadena Base64
    public static String getPublicKeyString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // Convierte la clave privada a una cadena Base64
    public static String getPrivateKeyString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    // Convierte una clave pública desde una cadena Base64
    public static PublicKey getPublicKeyFromString(String publicKeyString) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(publicKeyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new java.security.spec.X509EncodedKeySpec(decoded));
    }

    // Convierte una clave privada desde una cadena Base64
    public static PrivateKey getPrivateKeyFromString(String privateKeyString) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(privateKeyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(decoded));
    }

    // Cifra datos con la clave pública RSA
    public static byte[] encrypt(PublicKey publicKey, String data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data.getBytes());
    }

    // Descifra datos con la clave privada RSA
    public static String decrypt(PrivateKey privateKey, byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(encryptedData);
        return new String(decrypted);
    }

    // Firma un mensaje con la clave privada RSA
    public static byte[] sign(PrivateKey privateKey, String data) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        return signature.sign();
    }

    // Verifica una firma con la clave pública RSA
    public static boolean verify(PublicKey publicKey, String data, byte[] signatureBytes) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        return signature.verify(signatureBytes);
    }
}
