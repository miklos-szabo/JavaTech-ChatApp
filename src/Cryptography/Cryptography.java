package Cryptography;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Titkosítást megvalósító osztály
 * Konstansok állításával lehet az algoritmust állítani
 */
public abstract class Cryptography
{
    private static final String SIGNATURE = "SHA256withRSA";
    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private static Signature signature;
    private static KeyPairGenerator keyPairGenerator;
    private static KeyPair keyPair;
    private static Cipher cipher;
    private static byte[] encryptedText = null;

    /**
     * Visszaadja a használt kulcspárt
     * Célszerű inicializálás után használni, különben nullPointerException lesz
     * @return A használt kulcspár
     */
    public static KeyPair getKeyPair()
    {
        return keyPair;
    }

    /**
     * Inicializálja a titkosítást, létrehozza a kulcsokat például
     */
    public static void init()   //A szerver main függvényében hívjuk meg
    {
        try
        {
            signature = Signature.getInstance(SIGNATURE);
            keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
            cipher = Cipher.getInstance(TRANSFORMATION);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Kliens oldali inicializáció, mivel ugyanazt a kulcsot szeretnénk használni mindenhol,
     * ezért a kulcs alapján inicializálunk.
     * @param keys A szerver oldalon használt kulcspár
     */
    public static void initClient(KeyPair keys)    //Kliens oldalon hívjuk meg
    {
        try
        {
            signature = Signature.getInstance(SIGNATURE);
            keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(2048);
            keyPair = keys;
            cipher = Cipher.getInstance(TRANSFORMATION);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Titkosít egy {@link String}-et
     * @param string A titkosítandó {@link String}
     * @return A titkosított byte[].
     */
    public static byte[] encryptString(String string)
    {
        try
        {
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] input = string.getBytes();
            cipher.update(input);
            encryptedText = cipher.doFinal();
        }
        catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e)
        {
            e.printStackTrace();
        }
        return encryptedText;
    }

    /**
     * Feloldja egy byte[] titkosítását
     * @param input A korábban titkosított byte[]
     * @return A feloldott {@link String}
     */
    public static String decryptToString(byte[] input)
    {
        try
        {
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedText = cipher.doFinal(input);
            return new String(decryptedText, StandardCharsets.UTF_8);
        }
        catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
