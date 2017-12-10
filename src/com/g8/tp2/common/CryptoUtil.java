package com.g8.tp2.common;

import java.security.*;
import java.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.KeyGenerator;


public class CryptoUtil
{
    /*------------------------------------------------------
                           Start RSA part
    ------------------------------------------------------*/

    public static void generateRsaKeyPair(String serverName) throws NoSuchAlgorithmException
    {
        try
        {
            final int keySize = 4096;
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize);
            final KeyPair key = keyPairGenerator.genKeyPair();

            // Create the files on the root direct
            File privateKeyFile = new File("./private-" + serverName + ".key");
            File publicKeyFile = new File("./public-" + serverName + ".key");

            // Saving the Public key in a file
            ObjectOutputStream publicKeyOS = new ObjectOutputStream(
                    new FileOutputStream(publicKeyFile));
            publicKeyOS.writeObject(key.getPublic());
            publicKeyOS.close();

            // Saving the Private key in a file
            ObjectOutputStream privateKeyOS = new ObjectOutputStream(
                    new FileOutputStream(privateKeyFile));
            privateKeyOS.writeObject(key.getPrivate());
            privateKeyOS.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static byte[] encryptFromPrivateRsa(PrivateKey privateKey, String message) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] encryptFromPublicRsa(PublicKey publicKey, String message) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decryptWithPublicRsa(PublicKey publicKey, String encrypted) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(Base64.getDecoder().decode(encrypted));
    }

    public static byte[] decryptWithPrivateRsa(PrivateKey privateKey, String encrypted) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(Base64.getDecoder().decode(encrypted));
    }

    /*------------------------------------------------------
                           Start AES part
    ------------------------------------------------------*/

    public static SecretKey generateAesKey() throws Exception
    {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom()); //Using AES 256
        SecretKey secretKey = keyGen.generateKey();
        return secretKey;
    }

    public static IvParameterSpec generateInitVector(int blockSize)
    {
        //CBC uses 128-bit blocks, or 16-bytes
        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[blockSize];
        sr.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        return ivParams;
    }

    public static String encryptAES(String message, SecretKey sessionKey, IvParameterSpec iv) throws Exception
    {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //Using CBC mode
        c.init(Cipher.ENCRYPT_MODE, sessionKey, iv);
        byte[] encVal = c.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encVal);
    }

    public static String decryptAES(String encryptedMessage, SecretKey sessionKey, IvParameterSpec iv) throws Exception
    {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //Using CBC mode
        c.init(Cipher.DECRYPT_MODE, sessionKey, iv);
        byte[] base64Value = Base64.getDecoder().decode(encryptedMessage);
        byte[] decValue = c.doFinal(base64Value);
        return new String(decValue);
    }

    /*------------------------------------------------------
                           Start SHA part
    ------------------------------------------------------*/

    public static String hashMessage(String message) throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hash = digest.digest(message.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }

}
